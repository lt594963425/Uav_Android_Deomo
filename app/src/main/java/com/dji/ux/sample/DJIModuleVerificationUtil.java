package com.dji.ux.sample;

import androidx.annotation.Nullable;


import dji.common.product.Model;
import dji.sdk.accessory.AccessoryAggregation;
import dji.sdk.accessory.beacon.Beacon;
import dji.sdk.accessory.speaker.Speaker;
import dji.sdk.accessory.spotlight.Spotlight;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by dji on 16/1/6.
 */
public class DJIModuleVerificationUtil {


    public static boolean isProductModuleAvailable() {
        return (null != MApplication.getProductInstance());
    }

    public static boolean isAircraft() {
        return MApplication.getProductInstance() instanceof Aircraft;
    }

    public static boolean isHandHeld() {
        return MApplication.getProductInstance() instanceof HandHeld;
    }

    public static boolean isCameraModuleAvailable() {

        return isProductModuleAvailable() && (null != MApplication.getProductInstance().getCamera());
    }

    public static boolean isCameraModuleAvailableXT() {
        return isProductModuleAvailable() && (null != MApplication.getProductInstance().getCameras().get(1));
    }

    public static boolean isPlaybackAvailable() {
        return isCameraModuleAvailable() && (null != MApplication.getProductInstance()
                .getCamera()
                .getPlaybackManager());
    }

    public static boolean isMediaManagerAvailable() {
        return isCameraModuleAvailable() && (null != MApplication.getProductInstance()
                .getCamera()
                .getMediaManager());
    }

    public static boolean isRemoteControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != MApplication.getAircraftInstance()
                .getRemoteController());
    }

    public static boolean isRemoteMultiDevicePairingSupported() {
        return isProductModuleAvailable() && isAircraft()
                && (null != MApplication.getAircraftInstance().getRemoteController())
                && MApplication.getAircraftInstance().getRemoteController().isMultiDevicePairingSupported();
    }

    public static boolean isFlightControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && MApplication.getAircraftInstance() != null && (null != MApplication.getAircraftInstance().getFlightController());
    }

    //rtk判断
    public static boolean isRtkAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != MApplication.getAircraftInstance()
                .getFlightController()
                .getRTK());
    }

    //网络rtk
    public static boolean isNetRtkAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != DJISDKManager.getInstance().getRTKNetworkServiceProvider());
    }

    //基站电池
    public static boolean isBaseStationAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != ((Aircraft) MApplication.getProductInstance()).getBaseStation());
    }

    public static boolean isCompassAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != MApplication.getAircraftInstance()
                .getFlightController()
                .getCompass());
    }

    public static boolean isFlightLimitationAvailable() {
        return isFlightControllerAvailable() && isAircraft();
    }

    public static boolean isGimbalModuleAvailable() {
        return isProductModuleAvailable() && (null != MApplication.getProductInstance().getGimbal());
    }

    public static boolean isAirlinkAvailable() {
        return isProductModuleAvailable() && (null != MApplication.getProductInstance().getAirLink());
    }

    public static boolean isWiFiLinkAvailable() {
        return isAirlinkAvailable() && (null != MApplication.getProductInstance().getAirLink().getWiFiLink());
    }

    public static boolean isLightbridgeLinkAvailable() {

        return isAirlinkAvailable() && (null != MApplication.getProductInstance()
                .getAirLink()
                .getLightbridgeLink());
    }

    public static boolean isOcuSyncLinkLinkAvailable() {
        return isAirlinkAvailable() && (null != MApplication.getProductInstance().getAirLink().getOcuSyncLink());
    }


    public static AccessoryAggregation getAccessoryAggregation() {
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        if (aircraft != null && null != aircraft.getAccessoryAggregation()) {
            return aircraft.getAccessoryAggregation();
        }
        return null;
    }

    public static Speaker getSpeaker() {
        Aircraft aircraft = (Aircraft) MApplication.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getSpeaker()) {
            return aircraft.getAccessoryAggregation().getSpeaker();
        }
        return null;
    }

    public static Beacon getBeacon() {
        Aircraft aircraft = (Aircraft) MApplication.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getBeacon()) {
            return aircraft.getAccessoryAggregation().getBeacon();
        }
        return null;
    }

    public static Spotlight getSpotlight() {
        Aircraft aircraft = (Aircraft) MApplication.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getSpotlight()) {
            return aircraft.getAccessoryAggregation().getSpotlight();
        }
        return null;
    }

    @Nullable
    public static Simulator getSimulator() {
        Aircraft aircraft = MApplication.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                return flightController.getSimulator();
            }
        }
        return null;
    }

    @Nullable
    public static FlightController getFlightController() {
        Aircraft aircraft = MApplication.getAircraftInstance();
        if (aircraft != null) {
            return aircraft.getFlightController();
        }
        return null;
    }

    @Nullable
    public static boolean isMavic2Zoom() {
        BaseProduct baseProduct = MApplication.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MAVIC_2_ZOOM;
        }
        return false;
    }

    @Nullable
    public static boolean isMavic2ENTERPRISEDUALProduct() {
        BaseProduct baseProduct = MApplication.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MAVIC_2_ENTERPRISE_DUAL;
        }
        return false;
    }

    @Nullable
    public static boolean isMavic2ENTERPRISEProduct() {
        BaseProduct baseProduct = MApplication.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MAVIC_2_ENTERPRISE;
        }
        return false;
    }

    /**
     * 是否链接热像仪
     *
     * @return
     */
    public boolean isThermalCamera() {
        return DJIModuleVerificationUtil.isCameraModuleAvailable()
                && MApplication.getProductInstance().getCamera().isThermalCamera();
    }
}
