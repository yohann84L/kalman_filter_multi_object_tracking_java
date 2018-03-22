import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.inverse.InvertMatrix;

/**
 Kalman Filter class keeps track of the estimated state of
 the system and the variance or uncertainty of the estimate.
 Predict and Correct methods implement the functionality
 Reference: https://en.wikipedia.org/wiki/Kalman_filter
 Attributes: None
 */
public class KalmanFilter {

    private double dt = 0.005;  // delta time

    private INDArray A = Nd4j.create(new int[]{1,0,0,1}, new int[]{2,2}); // matrix in observation equations
    private INDArray u = Nd4j.zeros(new int[]{2,1}); // previous state vector

    // (x,y) tracking object center
    private INDArray b = Nd4j.create(new int[]{0,255}, new int[]{2,1}); // vector of observations
    private INDArray P = Nd4j.diag(Nd4j.create(new int[]{3,3}, new int[]{2,2})); // covariance matrix
    private INDArray F = Nd4j.create(new double[]{1.0, this.dt, 0.0, 1.0}, new int[]{2,2}); // state transition mat

    private INDArray Q = Nd4j.eye(u.shape()[0]); // process noise matrix
    private INDArray R = Nd4j.eye(b.shape()[0]); // observation noise matrix
    private INDArray lastResult = Nd4j.create(new int[]{0,255}, new int[]{2,1});




    /** Predict state vector u and variance of uncertainty P (covariance).
     where,
     u: previous state vector
     P: previous covariance matrix
     F: state transition matrix
     Q: process noise matrix
     Equations:
     u'_{k|k-1} = Fu'_{k-1|k-1}
     P_{k|k-1} = FP_{k-1|k-1} F.T + Q
     where,
     F.T is F transpose
     * @return vector of predicted state estimate
     */
    public INDArray predict() {
        // Predicted state estimate
        u = P.mmul(F.transpose());
        // Predicted estimate covariance
        P = Q.add(F.mmul(P.mmul(F.transpose())));
        lastResult = u; // same last predicted result

        return u;
    }



    /** Correct or update state vector u and variance of uncertainty P (covariance).
        where,
        u: predicted state vector u
        A: matrix in observation equations
        b: vector of observations
        P: predicted covariance matrix
        Q: process noise matrix
        R: observation noise matrix
        Equations:
            C = AP_{k|k-1} A.T + R
            K_{k} = P_{k|k-1} A.T(C.Inv)
            u'_{k|k} = u'_{k|k-1} + K_{k}(b_{k} - Au'_{k|k-1})
            P_{k|k} = P_{k|k-1} - K_{k}(CK.T)
            where,
                A.T is A transpose
                C.Inv is C inverse
     * @param b vector of observations
     * @param flag if "true" prediction result will be updated else detection
     * @return predicted state vector u
     */
    public INDArray correct(INDArray b, boolean flag) {
        if(!flag) { // update using prediction
            this.b = this.lastResult;
        } else {
            this.b = b;

            INDArray C = R.add(A.mmul(P.mmul(A.transpose())));
            INDArray K = P.mmul(A.transpose().mmul(InvertMatrix.invert(C, true)));

            u = u.add(K.mmul(b.neq(A.mmul(u))));
            P = P.neq(K.mmul(C.mmul(K.transpose())));
            lastResult = u;
        }

        return this.u;
    }
}
