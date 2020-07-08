package com.lsr.sensordatacollection;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorReadingsOptions implements Parcelable {

    private boolean recordAcc;
    private boolean recordGyro;
    private boolean recordMag;
    private boolean recordWifi;

    private String recordingsLocation;
    private String buildingTag;

    public SensorReadingsOptions(boolean recordAcc, boolean recordGyro,
                                 boolean recordMag, boolean recordWifi,
                                 String recordingsLocation, String buildingTag) {

        this.recordAcc = recordAcc;
        this.recordGyro = recordGyro;
        this.recordMag = recordMag;
        this.recordWifi = recordWifi;

        this.recordingsLocation = recordingsLocation;
        this.buildingTag = buildingTag;
    }

    public boolean isRecordAcc() {
        return recordAcc;
    }

    public boolean isRecordGyro() {
        return recordGyro;
    }

    public boolean isRecordMag() {
        return recordMag;
    }

    public boolean isRecordWifi() {
        return recordWifi;
    }

    public String getRecordingsLocation() {
        return recordingsLocation;
    }

    public String getBuildingTag() {
        return buildingTag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(recordingsLocation);
        parcel.writeString(buildingTag);

        if (recordAcc) {
            parcel.writeInt(1);
        }
        else {
            parcel.writeInt(0);
        }

        if (recordGyro) {
            parcel.writeInt(1);
        }
        else {
            parcel.writeInt(0);
        }

        if (recordMag) {
            parcel.writeInt(1);
        }
        else {
            parcel.writeInt(0);
        }

        if (recordWifi) {
            parcel.writeInt(1);
        }
        else {
            parcel.writeInt(0);
        }
    }

    public static final Parcelable.Creator<SensorReadingsOptions> CREATOR = new Parcelable.Creator<SensorReadingsOptions>() {
        public SensorReadingsOptions createFromParcel(Parcel in) {
            return new SensorReadingsOptions(in);
        }

        public SensorReadingsOptions[] newArray(int size) {
            return new SensorReadingsOptions[size];
        }
    };

    private SensorReadingsOptions(Parcel in) {
        recordingsLocation = in.readString();
        buildingTag = in.readString();

        int acc = in.readInt();
        recordAcc = acc == 1;

        int gyro = in.readInt();
        recordGyro = gyro == 1;

        int mag = in.readInt();
        recordMag = mag == 1;

        int wifi = in.readInt();
        recordWifi = wifi == 1;
    }
}
