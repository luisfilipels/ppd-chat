package networking;

import net.jini.space.JavaSpace;
import utils.ClientDataSingleton;
import utils.MathUtils;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;
import utils.tuples.UserTrackerTuple;
import utils.tuples.UserTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TupleSpaceManager {

    private final JavaSpace javaSpace;

    public TupleSpaceManager() {
        Lookup finder = new Lookup(JavaSpace.class);
        javaSpace = (JavaSpace) finder.getService();
        if (javaSpace == null) {
            System.out.println("Couldn't find JavaSpace! Exiting...");
            System.exit(0);
        }
    }

    public void loginUser() throws AcquireTupleException, WriteTupleException {
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();

        UserTuple template = new UserTuple(clientData.userNick);

        UserTuple userToEnter = takeUser(template, 6000);

        if (userToEnter != null) {
            // User exists
            System.out.println("User exists. Writing read data to client data");
            userToEnter.userName = clientData.userName;


        } else {
            // User does not exist, so create and proceed
            System.out.println("User does not exist. Writing client data to tuple");
            userToEnter = template;
            userToEnter.userName = clientData.userName;
            userToEnter.userNick = clientData.userNick;
        }

        userToEnter.isOnline = clientData.initialOnlineStatus;
        userToEnter.latitude = clientData.initialLatitude;
        userToEnter.longitude = clientData.initialLongitude;

        System.out.println("User entering: ");
        System.out.println(userToEnter);
        writeUser(userToEnter);
    }

    private void writeUser(UserTuple user) throws WriteTupleException {
        try {
            javaSpace.write(user, null, 1000 * 60 * 60);
        } catch (Exception e) {
            throw new WriteTupleException();
        }
    }

    public UserTuple readUser(UserTuple user, long timeout) throws AcquireTupleException {
        try {
            return (UserTuple) javaSpace.read(user, null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    private UserTuple takeUser(UserTuple user, long timeout) throws  AcquireTupleException {
        try {
            return (UserTuple) javaSpace.take(user, null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    public void writeUserTracker() throws WriteTupleException {
        try {
            UserTrackerTuple auctionTracker = new UserTrackerTuple();
            auctionTracker.userToIPList = new HashMap<>();
            javaSpace.write(auctionTracker, null, 60 * 60 * 1000);
        } catch (Exception e) {
            throw new WriteTupleException();
        }
    }

    public void updateUserProperties(Integer latitude, Integer longitude, Boolean isOnline) throws AcquireTupleException, WriteTupleException {
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        UserTuple myUser = takeUser(new UserTuple(clientData.userNick), 6000);
        if (myUser == null) {
            throw new AcquireTupleException();
        }

        if (latitude != null) myUser.latitude = latitude;
        if (longitude != null) myUser.longitude = longitude;
        if (isOnline != null) myUser.isOnline = isOnline;

        writeUser(myUser);
    }

    private void writeUserTracker(UserTrackerTuple tuple) throws WriteTupleException {
        try {
            javaSpace.write(tuple, null, 60 * 60 * 1000);
        } catch (Exception e) {
            throw new WriteTupleException();
        }
    }

    private UserTrackerTuple takeUserTracker(long timeout) throws AcquireTupleException {
        try {
            return (UserTrackerTuple) javaSpace.take(new UserTrackerTuple(), null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    public UserTrackerTuple readUserTracker(long timeout) throws AcquireTupleException {
        try {
            return (UserTrackerTuple) javaSpace.read(new UserTrackerTuple(), null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    private UserTrackerTuple prepareUserTrack() throws AcquireTupleException, WriteTupleException {
        UserTrackerTuple auctionTracker = takeUserTracker(5000);
        if (auctionTracker == null) {
            writeUserTracker();
            auctionTracker = takeUserTracker(5000);
            if (auctionTracker == null) {
                System.out.println("Could not acquire user tracker!");
                throw new AcquireTupleException();
            }
        } else {
            if (auctionTracker.userToIPList == null) {
                auctionTracker.userToIPList = new HashMap<>();
            }
        }
        return auctionTracker;
    }

    public boolean userTrackerExists() throws AcquireTupleException {
        return readUserTracker(5000) != null;
    }

    private void deleteUserFromTracker(String userID) throws AcquireTupleException, WriteTupleException {
        UserTrackerTuple auctionTracker = takeUserTracker(6000);
        if (auctionTracker == null) {
            System.out.println("Couldn't get auction tracker!");
            throw new AcquireTupleException();
        }
        auctionTracker.userToIPList.remove(userID);
        writeUserTracker(auctionTracker);
    }

    public boolean userIsReachable(String userID) {
        try {
            ClientDataSingleton clientData = ClientDataSingleton.getInstance();
            UserTuple myUser = readUser(new UserTuple(clientData.userNick), 6000);
            UserTuple otherUser = readUser(new UserTuple(userID), 6000);
            if (otherUser == null) throw new AcquireTupleException();

            return MathUtils.getEuclideanDistanceBetweenUsers(myUser, otherUser) <= clientData.detectionRadius
                    && otherUser.isOnline;
        } catch (AcquireTupleException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addSelfToTrackerIfUserExists(String myIP) {
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();

        try {
            UserTuple myUser = takeUser(new UserTuple(clientData.userNick), 6000);
            if (myUser == null) {
                System.out.println("Couldn't acquire user tuple!");
                throw new AcquireTupleException();
            }

            UserTrackerTuple tracker = takeUserTracker(6000);
            if (tracker == null) {
                System.out.println("Couldn't acquire tracker!");
                writeUser(myUser);
                throw new AcquireTupleException();
            }

            tracker.userToIPList.put(myUser.userNick, myIP + "|" + clientData.receivePort);

            writeUserTracker(tracker);
            writeUser(myUser);

            System.out.println("Updated user tracker");

        } catch (AcquireTupleException e) {
            e.printStackTrace();
        } catch (WriteTupleException e) {
            e.printStackTrace();
        }

    }

    public List<String> getNeighborhood() throws AcquireTupleException {
        UserTrackerTuple tracker = readUserTracker(6000);

        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        UserTuple myUser = readUser(new UserTuple(clientData.userNick), 6000);

        List<String> returnList = new ArrayList<>();

        for (String otherUserID : tracker.userToIPList.keySet()) {
            if (otherUserID.trim().equals(myUser.userNick.trim())) continue;
            System.out.println("other user: " + otherUserID + "   my user: " + myUser.userNick);

            UserTuple otherUser = readUser(new UserTuple(otherUserID), 6000);

            if (otherUser == null) {
                System.out.println("User not found");
                continue;
            }

            double distance = MathUtils.getEuclideanDistanceBetweenUsers(myUser, otherUser);

            if (distance <= clientData.detectionRadius) {
                returnList.add(otherUserID);
            }
        }

        return returnList;
    }

    public String getUserIP(String userID) throws AcquireTupleException {
        UserTrackerTuple userTracker = readUserTracker(6000);

        return userTracker.userToIPList.get(userID);
    }

    public void setMyselfToOffline() throws AcquireTupleException, WriteTupleException {
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        UserTuple myUser = takeUser(new UserTuple(clientData.userNick), 6000);

        myUser.isOnline = false;

        writeUser(myUser);
    }

    public boolean userIsInRange(String user) {
        try {
            ClientDataSingleton clientData = ClientDataSingleton.getInstance();
            UserTuple myUser = readUser(new UserTuple(clientData.userNick), 6000);
            UserTuple otherUser = readUser(new UserTuple(user), 6000);
            if (otherUser == null) throw new AcquireTupleException();

            return MathUtils.getEuclideanDistanceBetweenUsers(myUser, otherUser) <= clientData.detectionRadius;
        } catch (AcquireTupleException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean selfIsOnline() throws AcquireTupleException {
        UserTuple myUser = readUser(new UserTuple(ClientDataSingleton.getInstance().userNick), 6000);
        return myUser.isOnline;
    }

    public String getUserName(String contactNick) throws AcquireTupleException {
        UserTuple user = readUser(new UserTuple(contactNick), 6000);
        return user.userName;
    }
}
