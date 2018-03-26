import org.ejml.simple.SimpleMatrix;

/**
 Kalman Filter class keeps track of the estimated state of
 the system and the variance or uncertainty of the estimate.
 Predict and Correct methods implement the functionality
 Reference: https://en.wikipedia.org/wiki/Kalman_filter
 Attributes: None
 */
public class KalmanF {

    private double dt = 0.005;  // delta time

    private SimpleMatrix A;
    private SimpleMatrix u;

    // (x,y) tracking object center
    private SimpleMatrix b;
    private SimpleMatrix P;
    private SimpleMatrix F;

    private SimpleMatrix Q;
    private SimpleMatrix R;
    private SimpleMatrix lastResult;


    public KalmanF() {
        A = new SimpleMatrix(new double[][]{{1,0},{0,1}}); // matrix in observation equations
        u = new SimpleMatrix(2,1); // previous state vector

        b = new SimpleMatrix(new double[][]{{0},{255}}); // vector of observations
        P = new SimpleMatrix(new double[][]{{3,0},{0,3}}); // covariance matrix
        F = new SimpleMatrix(new double[][]{{1,dt},{0,1}}); // state transition mat

        Q = SimpleMatrix.identity(2); // process noise matrix
        R = SimpleMatrix.identity(2); // observation noise matrix
        lastResult = new SimpleMatrix(new double[][]{{0},{255}});
    }

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
    public SimpleMatrix predict() {
        // Predicted state estimate
        u = roundMatrix(F.mult(u));
        // Predicted estimate covariance
        P = Q.plus(F.mult(P.mult(F.transpose())));
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
    public SimpleMatrix correct(SimpleMatrix b, boolean flag) {
        if(!flag) { // update using prediction
            this.b = lastResult;
        } else {
            this.b = b;

            SimpleMatrix C = R.plus(A.mult(P.mult(A.transpose())));
            SimpleMatrix K = P.plus(A.transpose().mult(C.invert()));

            u = roundMatrix(u.plus(K.mult(b.minus(A.mult(u)))));
            P = P.minus(K.mult(C.mult(K.transpose())));
            lastResult = u;
        }

        return u;
    }

    public void setLastResult(SimpleMatrix lastResult) {
        this.lastResult = lastResult;
    }

    private SimpleMatrix roundMatrix(SimpleMatrix M) {
        for(int j = 0; j < M.numCols(); j++) {
            for(int i = 0; i < M.numRows(); i++) {
                M.set(i, j, Math.round(M.get(i, j)));
            }
        }
        return M;
    }
}
