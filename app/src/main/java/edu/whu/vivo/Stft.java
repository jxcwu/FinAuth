package edu.whu.vivo;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * created by Terence
 * on 2022/4/2
 */

class Stft {
    private int n_fft = 16;
    private int hop_length = 2;

    public double[][] extractSTFTFeatures(float[] y) {
        // Short-time Fourier transform (edu.whu.vivo.MainActivity.STFT)
        final double[] fftwin = getWindow();

        // pad y with reflect mode so it's centered. This reflect padding implementation
        // is
        final double[][] frame = padFrame(y, true);
        double[][] fftmagSpec = new double[1 + n_fft / 2][frame[0].length];

        double[] fftFrame = new double[n_fft];

        for (int k = 0; k < frame[0].length; k++) {
            int fftFrameCounter = 0;
            for (int l = 0; l < n_fft; l++) {
                fftFrame[fftFrameCounter] = fftwin[l] * frame[l][k];
                fftFrameCounter = fftFrameCounter + 1;
            }

            double[] tempConversion = new double[fftFrame.length];
            double[] tempImag = new double[fftFrame.length];

            FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);



            try {
                Complex[] complx = transformer.transform(fftFrame, TransformType.FORWARD);

                for (int i = 0; i < complx.length; i++) {
                    double rr = (complx[i].getReal());

                    double ri = (complx[i].getImaginary());

                    tempConversion[i] = rr * rr + ri*ri;
                    tempImag[i] = ri;
                }

            } catch (IllegalArgumentException e) {
                System.out.println(e);
            }



            double[] magSpec = tempConversion;
            for (int i = 0; i < 1 + n_fft / 2; i++) {
                fftmagSpec[i][k] = magSpec[i];
            }
        }
        return fftmagSpec;
    }

    private double[] getWindow() {
        // Return a Hann window for even n_fft.
        // The Hann window is a taper formed by using a raised cosine or sine-squared
        // with ends that touch zero.
        double[] win = new double[n_fft];
        for (int i = 0; i < n_fft; i++) {
            win[i] = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / n_fft);
        }
        return win;
    }

    private double[][] yFrame(double[] ypad) {

        final int n_frames = 1 + (ypad.length - n_fft) / hop_length;

        double[][] winFrames = new double[n_fft][n_frames];

        for (int i = 0; i < n_fft; i++) {
            for (int j = 0; j < n_frames; j++) {
                winFrames[i][j] = ypad[j * hop_length + i];
            }
        }
        return winFrames;
    }




    private double[][] padFrame(float[] yValues, boolean paddingFlag){

        double[][] frame = null;

        if(paddingFlag) {


            double[] ypad = new double[n_fft + yValues.length];
            for (int i = 0; i < n_fft / 2; i++) {
                ypad[(n_fft / 2) - i - 1] = yValues[i + 1];
                ypad[(n_fft / 2) + yValues.length + i] = yValues[yValues.length - 2 - i];
            }
            for (int j = 0; j < yValues.length; j++) {
                ypad[(n_fft / 2) + j] = yValues[j];
            }

            frame = yFrame(ypad);
        }
        else {


            double[] yDblValues = new double[yValues.length];
            for (int i = 0 ; i < yValues.length; i++)
            {
                yDblValues[i] = (double) yValues[i];
            }

            frame = yFrame(yDblValues);

        }

        return frame;
    }

}