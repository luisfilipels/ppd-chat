package utils;

import utils.tuples.UserTuple;

public class MathUtils {

    public static double getEuclideanDistanceBetweenUsers(UserTuple u1, UserTuple u2) {
        return Math.sqrt(Math.pow(u1.latitude - u2.latitude, 2) + Math.pow(u1.longitude - u2.longitude, 2));
    }

}
