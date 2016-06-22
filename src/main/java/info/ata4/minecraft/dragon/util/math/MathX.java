/*
 ** 2012 Januar 8
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.util.math;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.Random;

/**
 * Math helper class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MathX {

    public static final double PI_D = Math.PI;
    public static final float PI_F = (float) Math.PI;
    public static boolean useLUT = true;
    
    /**
     * You no take constructor!
     */
    private MathX() {
    }

    // float sine function, may use LUT
    public static float sin(float a) {
        if (useLUT) {
            return MathHelper.sin(a);
        } else {
            return (float) Math.sin(a);
        }
    }

    /** return a random value from a truncated gaussian distribution with
     *    mean and standard deviation = threeSigma/3
     *  distribution is truncated to +/- threeSigma.
     * @param mean the mean of the distribution
     * @param threeSigma three times the standard deviation of the distribution
     * @return
     */
    public static double getTruncatedGaussian(Random rand, double mean, double threeSigma)
    {
      double rawValue = rand.nextGaussian();
      rawValue = MathHelper.clamp_double(rawValue, -3.0, +3.0);
      return mean + rawValue * threeSigma / 3.0;
    }

    /** choose a random point on a sphere centred at the origin, with a given radius
     *  The points are evenly distributed over the surface of the sphere
     *  http://mathworld.wolfram.com/SpherePointPicking.html
     * @param rand
     * @param radius radius of the sphere
     * @return The point on the sphere
     */
    public static Vec3 getRandomPointOnSphere(Random rand, double radius)
    {
      Vec3 raw = new Vec3(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
      Vec3 result = MathX.multiply(raw.normalize(), radius);
      return result;
    }

    // float cosine function, may use LUT
    public static float cos(float a) {
        if (useLUT) {
            return MathHelper.cos(a);
        } else {
            return (float) Math.cos(a);
        }
    }

    // float tangent function
    public static float tan(float a) {
        return (float) Math.tan(a);
    }

    // float atan2 function
    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    // float degrees to radians conversion
    public static float toRadians(float angdeg) {
        return (float) Math.toRadians(angdeg);
    }

    // float radians to degrees conversion
    public static float toDegrees(float angrad) {
        return (float) Math.toDegrees(angrad);
    }
    
    // normalizes a float degrees angle to between +180 and -180
    public static float normDeg(float a) {
        a %= 360;
        if (a >= 180) {
            a -= 360;
        }
        if (a < -180) {
            a += 360;
        }
        return a;
    }
    
    // normalizes a double degrees angle to between +180 and -180
    public static double normDeg(double a) {
        a %= 360;
        if (a >= 180) {
            a -= 360;
        }
        if (a < -180) {
            a += 360;
        }
        return a;
    }
    
    // normalizes a float radians angle to between +π and -π
    public static float normRad(float a) {
        a %= PI_F * 2;
        if (a >= PI_F) {
            a -= PI_F * 2;
        }
        if (a < -PI_F) {
            a += PI_F * 2;
        }
        return a;
    }
    
    // normalizes a double radians angle to between +π and -π
    public static double normRad(double a) {
        a %= PI_D * 2;
        if (a >= PI_D) {
            a -= PI_D * 2;
        }
        if (a < -PI_D) {
            a += PI_D * 2;
        }
        return a;
    }
    
    // float square root
    public static float sqrtf(float f) {
        return (float) Math.sqrt(f);
    }
    
    // numeric float clamp
    public static float clamp(float value, float min, float max) {
        return (value < min ? min : (value > max ? max : value));
    }
    
    // numeric double clamp
    public static double clamp(double value, double min, double max) {
        return (value < min ? min : (value > max ? max : value));
    }
    
    // numeric integer clamp
    public static int clamp(int value, int min, int max) {
        return (value < min ? min : (value > max ? max : value));
    }
    
    // float linear interpolation
    public static float lerp(float a, float b, float x) {
        return a * (1 - x) + b * x;
    }
    
    // double linear interpolation
    public static double lerp(double a, double b, double x) {
        return a * (1 - x) + b * x;
    }
    
    // smoothed float linear interpolation, similar to terp() but faster
    public static float slerp(float a, float b, float x) {
        if (x <= 0) {
            return a;
        }
        if (x >= 1) {
            return b;
        }
        
        return lerp(a, b, x * x * (3 - 2 * x));
    }
    
    // smoothed double linear interpolation, similar to terp() but faster
    public static double slerp(double a, double b, double x) {
        if (x <= 0) {
            return a;
        }
        if (x >= 1) {
            return b;
        }
        
        return lerp(a, b, x * x * (3 - 2 * x));
    }
    
    // float trigonometric interpolation
    public static float terp(float a, float b, float x) {
        if (x <= 0) {
            return a;
        }
        if (x >= 1) {
            return b;
        }

        float mu2 = (1 - cos(x * PI_F)) / 2.0f;
        return a * (1 - mu2) + b * mu2;
    }
    
    // double trigonometric interpolation
    public static double terp(double a, double b, double x) {
        if (x <= 0) {
            return a;
        }
        if (x >= 1) {
            return b;
        }

        double mu2 = (1 - Math.cos(x * PI_D)) / 2.0;
        return a * (1 - mu2) + b * mu2;
    }

    /** clamp the target angle to within a given range of the centre angle
     * @param targetAngle the desired angle (degrees)
     * @param centreAngle the centre angle to clamp to (degrees)
     * @param maximumDifference the maximum allowable difference between the target and the centre (degrees)
     * @return the target angle, clamped to within +/-maximuDifference of the centreAngle.
     */
    public static float constrainAngle(float targetAngle, float centreAngle, float maximumDifference) {
        return centreAngle + clamp(normDeg(targetAngle - centreAngle), -maximumDifference, maximumDifference);
    }

    public static Vec3 multiply(Vec3 source, double multiplier)
    {
      return new Vec3(source.xCoord * multiplier, source.yCoord * multiplier, source.zCoord * multiplier);
    }

    public final static double MINIMUM_SIGNIFICANT_DIFFERENCE = 1e-3;

    public static boolean isApproximatelyEqual(double x1, double x2) {

      return Math.abs(x1 - x2) <= MINIMUM_SIGNIFICANT_DIFFERENCE;
    }

  public static boolean isSignificantlyDifferent(double x1, double x2)
  {
    return Math.abs(x1 - x2) > MINIMUM_SIGNIFICANT_DIFFERENCE;
  }

    /** return the modulus (always positive)
     * @param numerator
     * @param divisor
     * @return calculates the numerator modulus by divisor, always positive
     */
  public static int modulus(int numerator, int divisor) {
      return (numerator % divisor + divisor) % divisor;
  }

  // calculate the yaw from the given direction
  // returns from -180 to +180
  public static double calculateYaw(Vec3 direction)
  {
    double yaw = (Math.atan2(direction.zCoord, direction.xCoord) * 180.0D / Math.PI) - 90.0F;
    yaw = MathX.normDeg(yaw);
      return yaw;
  }

    // calculate the pitch from the given direction
    // returns from -90 to +90
    public static double calculatePitch(Vec3 direction)
    {
        double xz_norm = MathHelper.sqrt_double(direction.xCoord * direction.xCoord + direction.zCoord * direction.zCoord);
        double pitch = -(Math.atan2(direction.yCoord, xz_norm) * 180.0D / Math.PI);
        return pitch;
    }

    /**
     * Return a random integer in the given range
     * @param random
     * @param minValue minimum possible value (inclusive)
     * @param maxValue maximum possible value (exclusive)
     * @return the random value lying between [minimum, maximum]
     */
  public static int getRandomInRange(Random random, int minValue, int maxValue)
  {
    return random.nextInt(maxValue - minValue + 1) + minValue;
  }

    /**
     * Return a random float in the given range
     * @param random
     * @param minValue minimum possible value (inclusive)
     * @param maxValue maximum possible value (exclusive)
     * @return the random value lying between [minimum, maximum]
     */
    public static float getRandomInRange(Random random, float minValue, float maxValue)
    {
        return random.nextFloat() * (maxValue - minValue) + minValue;
    }

    /**
     * find the closest distance between the aabb to the given point
     * @param aabb the aabb
     * @param point the point to be measured to
     * @return the distance squared
     */
    public static double getClosestDistanceSQ(AxisAlignedBB aabb, Vec3 point)
    {
      // because the aabb is aligned, we just need to figure out the distance in each cardinal axis and then
      //  add them together
      double dx = Math.max(Math.max(0, aabb.minX - point.xCoord), point.xCoord - aabb.maxX);
      double dy = Math.max(Math.max(0, aabb.minY - point.yCoord), point.yCoord - aabb.maxY);
      double dz = Math.max(Math.max(0, aabb.minZ - point.zCoord), point.zCoord - aabb.maxZ);
      return dx*dx + dy*dy + dz*dz;
    }


}
