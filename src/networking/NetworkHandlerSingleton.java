package networking;

import net.jini.space.JavaSpace;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;
import utils.tuples.AuctionTrackerTuple;
import utils.tuples.BatchTuple;
import utils.tuples.UserTuple;

import java.util.ArrayList;
import java.util.List;

public class NetworkHandlerSingleton {

    private static NetworkHandlerSingleton instance;
    private final JavaSpace javaSpace;

    private NetworkHandlerSingleton() {
        Lookup finder = new Lookup(JavaSpace.class);
        javaSpace = (JavaSpace) finder.getService();
        if (javaSpace == null) {
            System.out.println("Couldn't find JavaSpace! Exiting...");
            System.exit(0);
        }
    }

    public static NetworkHandlerSingleton getInstance() {
        if (instance == null) {
            instance = new NetworkHandlerSingleton();
        }
        return instance;
    }

    public void loginUser(String userName) throws AcquireTupleException, WriteTupleException {
        UserTuple template = new UserTuple();
        template.userID = userName;

        UserTuple userToEnter = readUser(template, 6000);

        if (userToEnter != null) {
            // User exists
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userToEnter);
        } else {
            // User does not exist, so create and proceed
            userToEnter = template;
            userToEnter.madeAuctions = new ArrayList<>();
            writeUser(userToEnter);
            //TODO: Remove this commented code, if no longer necessary
            //UserTuple testTuple = readUser(template, 6000);
            //if (testTuple != null) System.out.println("Creating user");
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userToEnter);
        }
    }

    public UserTuple getMyUserWithHandler(NetworkHandlerSingleton networkHandler) throws AcquireTupleException {
        UserTuple template = new UserTuple();
        template.userID = ClientDataSingleton.getInstance().userName;
        UserTuple myUser = networkHandler.readUser(template, 6000);
        if (myUser == null) {
            System.out.println("Could not acquire myUser!");
            throw new AcquireTupleException();
        }
        return myUser;
    }

    private void updateUserWithNewAuction(String id) throws AcquireTupleException, WriteTupleException {
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        UserTuple template = new UserTuple(clientData.userName);

        UserTuple myUser = takeUser(template, 5000);
        if (myUser.madeAuctions == null) {
            myUser.madeAuctions = new ArrayList<>();
        }
        myUser.madeAuctions.add(id);
        writeUser(myUser);
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

    public void writeAuctionTracker() throws WriteTupleException {
        try {
            AuctionTrackerTuple auctionTracker = new AuctionTrackerTuple();
            auctionTracker.auctionList = new ArrayList<>();
            javaSpace.write(auctionTracker, null, 60 * 60 * 1000);
        } catch (Exception e) {
            throw new WriteTupleException();
        }
    }

    private void writeAuctionTracker(AuctionTrackerTuple tuple) throws WriteTupleException {
        try {
            javaSpace.write(tuple, null, 60 * 60 * 1000);
        } catch (Exception e) {
            throw new WriteTupleException();
        }
    }

    private AuctionTrackerTuple takeAuctionTracker(long timeout) throws AcquireTupleException {
        try {
            return (AuctionTrackerTuple) javaSpace.take(new AuctionTrackerTuple(), null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    public AuctionTrackerTuple readAuctionTracker(long timeout) throws AcquireTupleException {
        try {
            return (AuctionTrackerTuple) javaSpace.read(new AuctionTrackerTuple(), null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    public void writeAuction(BatchTuple tuple) throws WriteTupleException {
        try {
            javaSpace.write(tuple, null, 60 * 60 * 1000);
        } catch (Exception e) {
            throw new WriteTupleException();
        }
    }

    public void writeAuction(String auctionID, String description, String sellerID) throws WriteTupleException, AcquireTupleException {

        AuctionTrackerTuple auctionTracker = prepareAuctionTracker();
        auctionTracker.auctionList.add(auctionID);
        writeAuctionTracker(auctionTracker);

        updateUserWithNewAuction(auctionID);

        BatchTuple newAuction = new BatchTuple(auctionID, description, sellerID);
        try {
            javaSpace.write(newAuction, null, 60 * 60 * 1000);
        } catch (Exception e) {
            throw new WriteTupleException();
        }
    }

    private AuctionTrackerTuple prepareAuctionTracker() throws AcquireTupleException, WriteTupleException {
        AuctionTrackerTuple auctionTracker = takeAuctionTracker(5000);
        if (auctionTracker == null) {
            writeAuctionTracker();
            auctionTracker = takeAuctionTracker(5000);
            if (auctionTracker == null) {
                System.out.println("Could not acquire auction tracker!");
                throw new AcquireTupleException();
            }
        } else {
            if (auctionTracker.auctionList == null) {
                auctionTracker.auctionList = new ArrayList<>();
            }
        }
        return auctionTracker;
    }

    public BatchTuple readAuction(String auctionID) throws AcquireTupleException{
        try {
            BatchTuple template = new BatchTuple(auctionID, null, null, null);
            return (BatchTuple) javaSpace.read(template, null, 60 * 60 * 1000);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    public boolean auctionTrackerExists() throws AcquireTupleException {
        return readAuctionTracker(5000) != null;
    }

    public void deleteAuctionWithID(String auctionID) throws AcquireTupleException, WriteTupleException {
        deleteAuctionFromSelfUser(auctionID);
        deleteAuctionFromTracker(auctionID);
        takeAuctionTuple(auctionID, 6000);
    }

    private void deleteAuctionFromSelfUser(String auctionID) throws AcquireTupleException, WriteTupleException {
        UserTuple myUser = new UserTuple();
        myUser.userID = ClientDataSingleton.getInstance().userName;
        myUser = takeUser(myUser, 6000);
        if (myUser == null) throw new AcquireTupleException();
        if (myUser.madeAuctions == null) myUser.madeAuctions = new ArrayList<>();
        removeStringFromList(myUser.madeAuctions, auctionID);
        writeUser(myUser);
    }

    private void deleteAuctionFromTracker(String auctionID) throws AcquireTupleException, WriteTupleException {
        AuctionTrackerTuple auctionTracker = takeAuctionTracker(6000);
        if (auctionTracker == null) {
            System.out.println("Couldn't get auction tracker!");
            throw new AcquireTupleException();
        }
        removeStringFromList(auctionTracker.auctionList, auctionID);
        writeAuctionTracker(auctionTracker);
    }

    private void removeStringFromList(List<String> list, String str) {
        int indexToRemove = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(str)) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) list.remove(indexToRemove);
    }

    public BatchTuple takeAuctionTuple(String id, long timeout) throws AcquireTupleException {
        try {
            BatchTuple template = new BatchTuple(id, null, null, null);
            return (BatchTuple) javaSpace.take(template, null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

}
