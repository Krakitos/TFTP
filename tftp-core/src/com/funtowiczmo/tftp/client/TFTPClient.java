package com.funtowiczmo.tftp.client;

import com.funtowiczmo.tftp.client.observers.TFTPObservable;
import com.funtowiczmo.tftp.protocol.TFTPPacketFactory;
import com.funtowiczmo.tftp.protocol.enums.TFTPIOEnum;
import com.funtowiczmo.tftp.protocol.errors.TFTPException;
import com.funtowiczmo.tftp.protocol.packet.AcknowledgeablePacket;
import com.funtowiczmo.tftp.protocol.packet.SerializablePacket;
import com.funtowiczmo.tftp.protocol.packet.TFTPPacket;
import com.funtowiczmo.tftp.protocol.packet.impl.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Morgan on 03/04/2014.
 */
public class TFTPClient extends TFTPObservable {

    private final TFTPClientCommunicationHandler handler;
    private final BlockingQueue<TFTPPacket> packetQueue;

    private Thread backgroundThread;

    public TFTPClient(String remoteAddr) throws UnknownHostException, SocketException {
        this(InetAddress.getByName(remoteAddr), TFTPClientCommunicationHandler.DEFAULT_TFTP_PORT);
    }

    public TFTPClient(String remoteAddr, int port) throws UnknownHostException, SocketException {
        this(InetAddress.getByName(remoteAddr), port);
    }

    public TFTPClient(InetAddress remoteAddr) throws SocketException {
        this(remoteAddr, TFTPClientCommunicationHandler.DEFAULT_TFTP_PORT);
    }

    public TFTPClient(InetAddress remoteAddr, int port) throws SocketException {
        super();

        packetQueue = new LinkedBlockingQueue<TFTPPacket>();
        handler = new TFTPClientCommunicationHandler(remoteAddr, port, packetQueue);

        //Run thread
        backgroundThread = new Thread(handler);
        backgroundThread.start();
    }

    public int sendFile(File file, String remoteName, TFTPIOEnum mode){
        if(!file.exists()) return TFTPClientReturnCode.FILE_DOESNT_EXIST;

        fireFileSendingStarted(file);

        try {
            packetQueue.put(new WRQPacket(file, remoteName, mode));
        } catch (InterruptedException e) {
            e.printStackTrace();

            return TFTPClientReturnCode.THREAD_INTERRUPTION;
        }

        return TFTPClientReturnCode.RESULT_OK;
    }

    public int receiveFile(String remoteName, File storagePath, TFTPIOEnum mode){

        fireFileReceptionStarted(remoteName);

        try {
            packetQueue.put(new RRQPacket(storagePath, remoteName, mode));
        } catch (InterruptedException e) {
            e.printStackTrace();

            return TFTPClientReturnCode.THREAD_INTERRUPTION;
        }

        fireFileReceptionEnded(this, storagePath);

        return TFTPClientReturnCode.RESULT_OK;
    }

    public boolean isConnected(){
        return handler == null ? false : handler.isConnected();
    }

    public void close(){
        packetQueue.clear();
        handler.stop();
    }

    private class TFTPClientCommunicationHandler implements Runnable{

        private final Logger logger = LogManager.getLogManager().getLogger("");

        private static final int DEFAULT_PACKET_SIZE = 516;
        private static final int DEFAULT_DATA_SIZE = DEFAULT_PACKET_SIZE - 4;
        private static final int DEFAULT_TFTP_PORT = 69;

        private InetAddress serverAddress;
        private DatagramSocket socket;
        private AtomicBoolean run = new AtomicBoolean(true);

        private int server_tid = DEFAULT_TFTP_PORT;
        private final int client_tid;


        private final byte[] buffer = new byte[DEFAULT_PACKET_SIZE];

        //Producer - Consumer pattern, blocking the consumer thread if nothing to consume
        private final BlockingQueue<TFTPPacket> packetQueue;


        public TFTPClientCommunicationHandler(InetAddress remoteAddr, int port, BlockingQueue<TFTPPacket> packetQueue) throws SocketException {
            super();

            this.packetQueue = packetQueue;

            socket = new DatagramSocket();
            socket.setSoTimeout(30000); //30s timeout
            serverAddress = remoteAddr;
            client_tid = socket.getLocalPort();
        }

        @Override
        public void run() {

            //Go on while run is true
            while(run.get()) {
                try {
                    TFTPPacket packet = packetQueue.take();

                    if(packet instanceof WRQPacket){
                        sendFile((WRQPacket) packet);
                    }else if(packet instanceof RRQPacket){
                        receiveFile((RRQPacket) packet);
                    }

                } catch (Exception e) {
                    fireExceptionOccurred(TFTPClient.this, e);
                }
            }

            socket.close();
        }

        private void receiveFile(RRQPacket packet) throws IOException, TFTPException {
            //Response holder
            TFTPPacket response = null;

            //Loop until we get the response from the server (Data or Error packet)
            do {
                sendPacket(packet);
                response = waitResponse(); //waitReponse() will return null if socket timeout occurred
            }while(response == null);

            //RRQ packet is acknowledged by a Data packet or Error packet
            if(response instanceof DataPacket){
                boolean isFinal = false;
                FileOutputStream outputStream = null;

                //Try open the output stream to store data
                try {
                    outputStream = new FileOutputStream(packet.getLocalStorageFile());

                    do {
                        //Write last data received
                        outputStream.write(((DataPacket) response).getData(), 0, ((DataPacket) response).getLength());

                        //Ack packet
                        sendPacket(new AckPacket(((DataPacket) response).getBlockNumber()));

                        //Terminate reception if error packet is received or is data packet length is less than 512 bytes
                        isFinal = response instanceof ErrorPacket || (response instanceof DataPacket && ((DataPacket)response).getLength() < DEFAULT_DATA_SIZE);

                        //If packet is last, don't need to wait response.
                        if(!isFinal){
                            response = waitResponse();
                        }
                    }while(!isFinal);
                }finally {
                    if(outputStream != null) outputStream.close();

                    fireFileReceptionEnded(TFTPClient.this, packet.getLocalStorageFile());
                }

            }else if(response instanceof ErrorPacket){ //Error packet handler
                //Get the error packet
                ErrorPacket error = (ErrorPacket) response;

                //Inform observers
                fireProtocolErrorOccurred(TFTPClient.this, error.getErrno(), error.getErrorMsg());
            }
        }

        private void sendFile(WRQPacket packet) throws IOException, TFTPException {
            //Response holder
            TFTPPacket response = null;

            //Loop until we get the response from the server to send or abort (error) the sending process
            do {
                sendPacket(packet);
                response = waitResponse();
            }while(response == null);

            //If we receive an acknowledgement
            if(response instanceof AckPacket){

                //Acknowledgement has to have blockID = 0
                if(((AckPacket) response).getBlockID() == 0){

                    //We can start the upload
                    logger.info("Ack packet received with right block id, starting upload");

                    //Data buffer for string the file's data
                    byte[] data = null;

                    if(packet.getMode() == TFTPIOEnum.OCTET) {
                        //Read file input stream
                        FileInputStream stream = null;
                        try{
                            stream = new FileInputStream(packet.getLocalStorage());

                            //Get buffer for storing the file's data
                            data = new byte[(int) packet.getLocalStorage().length()];

                            //Read file's data
                            stream.read(data);

                        }finally {
                            if(stream != null) stream.close();
                        }

                    }else {

                        //Get char buffer
                        char[] chars = new char[(int) packet.getLocalStorage().length()];

                        //Create BufferedReader, and read file data
                        //Use InputStreamReader to set charset, FileReader assuming the system charset is suitable, which is OS dependent...
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(packet.getLocalStorage()),Charset.forName("ASCII")));
                        reader.read(chars, 0, chars.length);

                        //Create String with chars
                        final String s = new String(chars);

                        //Convert to ascii
                        data = s.getBytes(Charset.forName("ASCII"));

                        reader.close();
                    }

                    //Split packet to match packet max size
                    Queue<TFTPPacket> dataPackets = splitData(data);

                    int nbPackets = dataPackets.size();

                    //Send all packets
                    while(dataPackets.size() > 0){

                        //Take first one, Don't remove the packet at this point, because we may have to send it again if no ack received
                        TFTPPacket current = dataPackets.element();

                        //Send it
                        sendPacket(current);

                        //Wait the response
                        response = waitResponse();

                        //Check response
                        if(response instanceof AckPacket && current instanceof AcknowledgeablePacket){

                            //If blockID match on both side, remove the packet from the queue, go next !
                            if(((AcknowledgeablePacket)current).getBlockNumber() == ((AckPacket) response).getBlockID()){
                                //Remove from queue
                                dataPackets.poll();

                                //Inform observer for progression
                                fireFileSendingProgress((float)(nbPackets - dataPackets.size()) / (float)nbPackets);
                            }
                        }

                    }

                    fireFileSendingEnded(TFTPClient.this, packet.getLocalStorage());
                }
            }else if(response instanceof ErrorPacket){ //If error, inform observers and abort
                ErrorPacket error = ((ErrorPacket)response);
                fireProtocolErrorOccurred(TFTPClient.this, error.getErrno(), error.getErrorMsg());
            }
        }

        private Queue<TFTPPacket> splitData(byte[] data) {
            Queue<TFTPPacket> packets = new LinkedList<TFTPPacket>();
            int ptr = 0;
            short blockID = 1;

            while(ptr < data.length){
                packets.add(new DataPacket(blockID, data, ptr, ptr + DEFAULT_DATA_SIZE < data.length ? DEFAULT_DATA_SIZE : data.length - ptr));

                //If we go over Short.MAX_VALUE, we will have value = Short.MIN_VALUE ( -16,XXX) => So force positive value
                blockID = (short) (blockID + 1 < 0 ? 1 : blockID + 1);

                ptr += DEFAULT_DATA_SIZE;
            }

            return packets;
        }

        private TFTPPacket waitResponse() throws IOException, TFTPException {
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            //Wait for response
            //do{
                socket.receive(datagramPacket);
                logger.info("Raw packet received : " + datagramPacket.getAddress() + ":" + datagramPacket.getPort() + " / [SERVER_TID : " + server_tid + "]");
            //}while(datagramPacket.getPort() != server_tid || server_tid == DEFAULT_TFTP_PORT); //Discard any packet we don't know the sender

            if(server_tid == DEFAULT_TFTP_PORT){
                server_tid = datagramPacket.getPort();
            }

            //Get the packet from the factory
            TFTPPacket packet = TFTPPacketFactory.fromRawData(datagramPacket.getData());

            //Need to set the packet length for DataPacket to detect ending download
            if(packet instanceof DataPacket){
                logger.info("Packet length : " + datagramPacket.getLength());
                ((DataPacket) packet).setLength(datagramPacket.getLength() - 4); //4 bytes of opcode | blockNumber
            }

            //Deserialize it
            packet.deserializePacket(datagramPacket.getData());

            logger.info("Received : " + packet);

            //Return it
            return packet;
        }
        private void sendPacket(SerializablePacket packet) throws IOException {
            logger.info("Sending : " + packet);

            byte[] data = packet.serializePacket();

            DatagramPacket datagram = new DatagramPacket(data, data.length);
            datagram.setAddress(serverAddress);
            datagram.setPort(server_tid);
            socket.send(datagram);
        }

        public boolean isConnected(){
            return socket.isConnected();
        }

        public void stop(){
            run.set(false);
        }
    }
}
