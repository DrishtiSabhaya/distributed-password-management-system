package client;

import server.server.ClientServer;
import util.ClientLogger;
import util.Logger;
import util.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.regex.PatternSyntaxException;

/**
 * Class that represents a client facing application that communicates with
 * remote servers in a distributed system.
 */
public class Client {
    private static final Logger logger = new ClientLogger();
    /**
     * Application entry point. Requires two arguments. A port
     * and a host address of a server to connect respectively.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        String portString = args[0];

        if (!Utility.isValidPort(portString)) {
            System.out.println("Please enter a valid port number");
            logger.log("Invalid port number entered.");
            return;
        }

        int port = Integer.parseInt(portString);
        String host = args[1];

        ClientServer server;
        try {
            server = (ClientServer) Naming
                    .lookup(String.format("rmi://%s:%d/Service", host, port));;
        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Failed to connect to server. " +
                "Please ensure that you have the correct " +
                "host address and port number of the server you want to connect to.");
            logger.log("Failed to establish a remote connection with the registry. " + e);
            return;
        }   catch (NotBoundException e) {
            System.out.println("Failed to connect to server. " +
                "Please ensure that you have the correct " +
                "host address and port number of the server you want to connect to.");
            logger.log("The lookup name provided is not bound to any remote object" + e);
            return;
        }

        System.out.println("Please enter 's' to sign up, 'l' to login or 'q' to quit. " +
                "Your entry is case insensitive.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        server.entities.Client user = null;
        while (true) {
            try {
                input = reader.readLine();
                if (input.equalsIgnoreCase("q")) {
                    System.out.println("Application quit.");
                    return;
                } else if (input.equalsIgnoreCase("s")) {
                    user = handleSignup(reader, server);
                    System.out.println("User creation successful.");
                    System.out.println("Welcome " + user.username);
                    logger.log(String.format("User with name: %s created successfully", user.username));
                    break;
                } else if (input.equalsIgnoreCase("l")) {
                    user = handleLogin(reader, server);
                    System.out.println("Login successful.");
                    System.out.println("Welcome " + user.username);
                    logger.log(String.format("User with name: %s logged in successfully", user.username));
                    break;
                } else {
                    System.out.println("Invalid input.");
                }
            } catch (IOException e) {
                System.out.println("Failed to read user input.");
                logger.log("Exception occurred while reading user input");
            }
        }
        startPostLogin(reader, server, user);
    }

    private static void startPostLogin(BufferedReader reader, ClientServer server, server.entities.Client user) {
        System.out.println(getInstruction());
        System.out.println("Enter q at anytime to quit.");
        String request = "";

        while (true) {
            System.out.println("Please enter your request:");
            try {
                request = reader.readLine();
                if (request.equalsIgnoreCase("q")) {
                    System.exit(0);
                }

                Validation validation = validateInput(request);
                if (!validation.isValid()) {
                    System.out.println(validation.getMessage());
                    continue;
                }

                String[] input = request.split(",");
                String method = input[0];

                if (method.equalsIgnoreCase("get")) {
                    logger.log("GET request made by user " + user.username +
                        ". The request is " + request);
                    String result = server.GetPassword(user, input[1]);
                    logger.log("Response received from server: " + result);
                    if (result == null) {
                        System.out.println("There is no password associated with the name "
                            + input[1] + " for " + "user " + user.username);
                    } else {
                        System.out.println("Password: " + result);
                    }
                } else if (method.equalsIgnoreCase("put")) {
                    logger.log("PUT request made by user " + user.username +
                        ". The request is " + request);
                    boolean successful = server.PutPassword(user, input[1], input[2]);
                    logger.log("Response received from server: " + successful);
                    if (successful) {
                        System.out.println("PUT successful. Key = "
                            + input[1] + " Value = " + input[2]);
                    } else {
                        System.out.println("PUT failed.");
                    }
                } else {
                    logger.log("DELETE request made by user " + user.username
                        + ". The request is " + request);
                    boolean successful = server.DeletePassword(user, input[1]);
                    logger.log("Response received from server: " + successful);
                    if (successful) {
                        System.out.println("Delete successful. Key = " + input[1]);
                    } else {
                        System.out.println("There is no password associated with the name "
                            + input[1] + " for " + "user " + user.username);
                    }
                }
            } catch (IOException e) {
                logger.log("Failed to read user input. " + e);
            }
        }
    }

    /**
     * Validates that request follows the standard format.
     *
     * @param input input string to be validated
     * @return validation object
     */
    private static Validation validateInput(String input) {
        Validation result = new Validation();
        String[] arr;
        try {
            arr = input.split(",");
            if (arr.length <= 0) {
                result.setMessage("Invalid request. Missing required arguments");
                return result;
            }
        } catch (PatternSyntaxException e) {
            result.setMessage("Invalid request. Please read instruction and follow correct format.");
            return result;
        }

        result = isValidRequest(arr[0], arr);
        return result;
    }

    /**
     * Performs method level validation on request. For example a get request can
     * have only one argument while a put request can have 2.
     *
     * @param method  method in which validation is based upon
     * @param input input string to be validated
     * @return validation dto containing outcome of validation
     */
    private static Validation isValidRequest(String method, String[] input) {
        Validation result = new Validation(true, "Valid request");
        switch (method.toUpperCase()) {
            case "GET":
            case "DELETE":
                if (input.length < 2) {
                    result.setValid(false);
                    result.setMessage("One argument required for "
                        + method.toUpperCase() + " request: Key");
                }
                return result;
            case "PUT":
                if (input.length < 3) {
                    result.setValid(false);
                    result.setMessage("Two arguments required for PUT request: Key and Value");
                }
                return result;
            default:
                result.setValid(false);
                result.setMessage("Invalid request");
                return result;
        }
    }

    /**
     * Helper method to display instruction.
     *
     * @return instruction
     */
    private static String getInstruction() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "Please enter your input. The server accepts requests in the " +
                    "form <METHOD>,<KEY>,<VALUE>.\n");
        sb.append("Three method types are allowed namely: GET, PUT and DELETE.\n");
        sb.append(
                "Get and delete requests do not require a value. " +
                    "Example usage: 'GET,Instagram', 'PUT,Instagram,PasswordOfInsta99', ");
        sb.append("'DELETE,Instagram'. " +
            "\nWhite spaces are not ignored and will be a part of your key/value.");
        return sb.toString();
    }

    /**
     * Helper method to handle user creation.
     *
     * @param reader reader used to collect user input
     * @param server remote server object
     * @return newly created user
     */
    private static server.entities.Client handleSignup(BufferedReader reader, ClientServer server) {
        System.out.println("Sign up initiated. Please note that the letter 'q' is " +
            "reserved for quiting the application, and cannot be used as a username or password. " +
            "The letter l is reserved for switching to login and cannot be used as a " +
            "username or password");
        server.entities.Client u = null;
        while (true) {
            try {
                System.out.println("Enter username: ");
                String username = reader.readLine();
                if (username.equalsIgnoreCase("q")) {
                    System.out.println("Application quit.");
                    System.exit(0);
                }
                if (username.equalsIgnoreCase("l")) {
                    System.out.println("Switching to login...");
                    return handleLogin(reader, server);
                }
                if (username.trim().equals("")) {
                    System.out.println("Invalid username");
                    continue;
                }
                System.out.println("Enter password: ");
                String password = reader.readLine();
                if (password.equalsIgnoreCase("q")) {
                    System.out.println("Application quit.");
                    System.exit(0);
                }
                u = server.signUp(username, password);
                if (u == null) {
                    System.out.println("This username is already taken.");
                    continue;
                }
                break;
            } catch (IOException e) {
                System.out.println("Failed to read user input.");
                logger.log("Exception occurred while reading user input");
            }
        }
        return u;
    }

    /**
     * Helper method to handle user login.
     *
     * @param reader reader used to collect user input
     * @param server remote server object
     * @return logged in user
     */
    private static server.entities.Client handleLogin(BufferedReader reader, ClientServer server) {
        System.out.println("Login initiated. Please note that the letter 'q' is reserved " +
            "for quiting the application and cannot be used as a username or password. " +
            "The letter s is reserved for switching to sign up and cannot be used as a " +
            "username or password");
        server.entities.Client u = null;
        while (true) {
            try {
                System.out.println("Enter username: ");
                String username = reader.readLine();
                if (username.equalsIgnoreCase("q")) {
                    System.out.println("Application quit.");
                    System.exit(0);
                }
                if (username.equalsIgnoreCase("s")) {
                    System.out.println("Switching to sign up...");
                    return handleSignup(reader, server);
                }
                System.out.println("Enter password: ");
                String password = reader.readLine();
                if (password.equalsIgnoreCase("q")) {
                    System.out.println("Application quit.");
                    System.exit(0);
                }
                u = server.login(username, password);
                if (u == null) {
                    System.out.println("Invalid username or password");
                    continue;
                }
                break;
            } catch (IOException e) {
                System.out.println("Failed to read user input.");
                logger.log("Exception occurred while reading user input");
            }
        }
        return u;
    }
}
