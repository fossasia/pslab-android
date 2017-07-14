package org.fossasia.pslab.communication.sensors;

/**
 * Created by akarshan on 4/21/17.
 */

public class KalmanFilter {
    private double processVariance;
    private double estimatedMeasurementVariance;
    private double posteriEstimate;
    private double posteriErrorEstimate;
    private double prioriEstimate;
    private double prioriErrorEstimate;
    private double blendingFactor;

    public KalmanFilter(double processVariance, double estimatedMeasurementVariance) {
        this.processVariance = processVariance;
        this.estimatedMeasurementVariance = estimatedMeasurementVariance;
        posteriEstimate = 0.0;
        posteriErrorEstimate = 1.0;
    }

    void inputLatestNoisyMeasurement(double measurement) {
        prioriEstimate = posteriEstimate;
        prioriErrorEstimate = posteriErrorEstimate + processVariance;
        blendingFactor = prioriErrorEstimate / (prioriErrorEstimate + estimatedMeasurementVariance);
        posteriEstimate = prioriEstimate + blendingFactor * (measurement - prioriEstimate);
        posteriErrorEstimate = (1 - blendingFactor) * prioriErrorEstimate;
    }

    double getLatestEstimatedMeasurement() {
        return posteriEstimate;
    }
}
