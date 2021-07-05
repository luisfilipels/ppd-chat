package networking;

import net.jini.space.JavaSpace;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;
import utils.tuples.UserTrackerTuple;
import utils.tuples.UserTuple;

import java.util.HashMap;

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

        UserTuple template = new UserTuple(clientData.userID);

        UserTuple userToEnter = takeUser(template, 6000);

        if (userToEnter != null) {
            // User exists
            userToEnter.userNick = clientData.userNick;

            clientData.initialOnlineStatus = userToEnter.isOnline;
            clientData.initialLatitude = userToEnter.latitude;
            clientData.initialLongitude = userToEnter.longitude;
        } else {
            // User does not exist, so create and proceed
            userToEnter = template;
            userToEnter.userNick = clientData.userNick;
            userToEnter.userID = clientData.userID;;

            userToEnter.isOnline = clientData.initialOnlineStatus;
            userToEnter.latitude = clientData.initialLatitude;
            userToEnter.longitude = clientData.initialLongitude;
        }
        writeUser(userToEnter);
        ClientDataSingleton.getInstance().saveUserDataFromTuple(userToEnter);
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

    public void addSelfToTrackerIfUserExists() {
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();

        try {
            UserTuple myUser = takeUser(new UserTuple(clientData.userID), 6000);
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

            tracker.userToIPList.put(myUser.userID, "");

            writeUserTracker(tracker);

        } catch (AcquireTupleException e) {
            e.printStackTrace();
            return;
        } catch (WriteTupleException e) {
            e.printStackTrace();
        }

    }
}
