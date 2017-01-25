/*
 ** 2012 Januar 21
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.model.anim;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.client.model.ModelPart;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.DragonAnimatorCommon;
import info.ata4.minecraft.dragon.server.entity.helper.DragonHelper;
import info.ata4.minecraft.dragon.util.math.Interpolation;
import info.ata4.minecraft.dragon.util.math.MathX;

/**
 * Animation control class to put useless reptiles in motion.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonAnimatorClientOnly extends DragonAnimatorCommon {

    // final X rotation angles for ground
    private float[] xGround = {0, 0, 0, 0};

    // X rotation angles for ground
    // 1st dim - front, hind
    // 2nd dim - thigh, crus, foot, toe
    private float[][] xGroundStand = {
        {0.8f, -1.5f, 1.3f, 0},
        {-0.3f, 1.5f, -0.2f, 0},
    };
    private float[][] xGroundSit = {
        {0.3f, -1.8f, 1.8f, 0},
        {-0.8f, 1.8f, -0.9f, 0},
    };

    // X rotation angles for walking
    // 1st dim - animation keyframe
    // 2nd dim - front, hind
    // 3rd dim - thigh, crus, foot, toe
    private float[][][] xGroundWalk = {{
        {0.4f, -1.4f, 1.3f, 0},    // move down and forward
        {0.1f, 1.2f, -0.5f, 0}     // move back
    }, {
        {1.2f, -1.6f, 1.3f, 0},    // move back
        {-0.3f, 2.1f, -0.9f, 0.6f} // move up and forward
    }, {
        {0.9f, -2.1f, 1.8f, 0.6f}, // move up and forward
        {-0.7f, 1.4f, -0.2f, 0}    // move down and forward
    }};

    // final X rotation angles for walking
    private float[] xGroundWalk2 = {0, 0, 0, 0};

    // Y rotation angles for ground, thigh only
    private float[] yGroundStand = {-0.25f, 0.25f};
    private float[] yGroundSit = {0.1f, 0.35f};
    private float[] yGroundWalk = {-0.1f, 0.1f};

    // final X rotation angles for air
    private float[] xAir;

    // X rotation angles for air
    // 1st dim - front, hind
    // 2nd dim - thigh, crus, foot, toe
    private float[][] xAirAll = {{0, 0, 0, 0}, {0, 0, 0, 0}};

    // Y rotation angles for air, thigh only
    private float[] yAirAll = {-0.1f, 0.1f};

    public DragonAnimatorClientOnly(EntityTameableDragon dragon) {
        super(dragon);
    }


    public void animLegs(DragonModel model) {
        // dangling legs for flying
        if (getGroundTime() < 1) {
            float footAirOfs = getCycleOfs() * 0.1f;
            float footAirX = 0.75f + getCycleOfs() * 0.1f;

            xAirAll[0][0] = 1.3f + footAirOfs;
            xAirAll[0][1] = -(0.7f * getSpeed() + 0.1f + footAirOfs);
            xAirAll[0][2] = footAirX;
            xAirAll[0][3] = footAirX * 0.5f;

            xAirAll[1][0] = footAirOfs + 0.6f;
            xAirAll[1][1] = footAirOfs + 0.8f;
            xAirAll[1][2] = footAirX;
            xAirAll[1][3] = footAirX * 0.5f;
        }
        
        // 0 - front leg, right side
        // 1 - hind leg, right side
        // 2 - front leg, left side
        // 3 - hind leg, left side
        for (int i = 0; i < model.thighProxy.length; i++) {
            ModelPart thigh, crus, foot, toe;
            
            if (i % 2 == 0) {
                thigh = model.forethigh;
                crus = model.forecrus;
                foot = model.forefoot;
                toe = model.foretoe;
                
                thigh.rotationPointZ = 4;
            } else {
                thigh = model.hindthigh;
                crus = model.hindcrus;
                foot = model.hindfoot; 
                toe = model.hindtoe;
                
                thigh.rotationPointZ = 46;
            }
            
            xAir = xAirAll[i % 2];
            
            // interpolate between sitting and standing
            slerpArrays(xGroundStand[i % 2], xGroundSit[i % 2], xGround, getSitTime());
            
            // align the toes so they're always horizontal on the ground
            xGround[3] = -(xGround[0] + xGround[1] + xGround[2]);
            
            // apply walking cycle
            if (getWalkTime() > 0) {
                // interpolate between the keyframes, based on the cycle
                splineArrays(getMoveTime() * 0.2f, i > 1, xGroundWalk2,
                        xGroundWalk[0][i % 2], xGroundWalk[1][i % 2], xGroundWalk[2][i % 2]);
                // align the toes so they're always horizontal on the ground
                xGroundWalk2[3] -= xGroundWalk2[0] + xGroundWalk2[1] + xGroundWalk2[2];
                
                slerpArrays(xGround, xGroundWalk2, xGround, getWalkTime());
            }
            
            float yAir = yAirAll[i % 2];
            float yGround;
            
            // interpolate between sitting and standing
            yGround = Interpolation.smoothStep(yGroundStand[i % 2], yGroundSit[i % 2], getSitTime());
            
            // interpolate between standing and walking
            yGround = Interpolation.smoothStep(yGround, yGroundWalk[i % 2], getWalkTime());
            
            // interpolate between flying and grounded
            thigh.rotateAngleY = Interpolation.smoothStep(yAir, yGround, getGroundTime());
            thigh.rotateAngleX = Interpolation.smoothStep(xAir[0], xGround[0], getGroundTime());
            crus.rotateAngleX = Interpolation.smoothStep(xAir[1], xGround[1], getGroundTime());
            foot.rotateAngleX = Interpolation.smoothStep(xAir[2], xGround[2], getGroundTime());
            toe.rotateAngleX = Interpolation.smoothStep(xAir[3], xGround[3], getGroundTime());
            
            // update proxy
            model.thighProxy[i].update();
        }
    }
}
