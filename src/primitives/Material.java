package primitives;

/***
 * contain the attenuation and shininess factors
 */
public class Material {
    /**
     * The level of diffuse of the material
     */
    private Double3 kD = Double3.ZERO;
    /**
     *The level of specular of the material
     */
    private Double3 kS = Double3.ZERO;
    /**
     * Promotes transparency
     */
    public Double3 kT= Double3.ZERO;
    /**
     * Coefficient of reflection
     */
    public Double3 kR= Double3.ZERO;
    /**
     *The level of shininess of the material
     */
    private int nShininess = 0;

    /**
     * update kT value
     * @param kT transparency value
     * @return this
     */
    public Material setKt(double kT) {
        this.kT =new Double3(kT) ;
        return this;
    }

    /**
     * update kR value
     * @param kR reflection value
     * @return this
     */
    public Material setKr(double kR) {
        this.kR = new Double3(kR);
        return this;
    }


    /***
     * set the factor d
     * @param kD object double
     * @return this
     */
    public Material setkD(double kD) {
        this.kD = new Double3(kD);
        return this;
    }

    /***
     * set the factor d
     * @param kd object Double3
     * @return this
     */
    public Material setkD(Double3 kd) {
        this.kD = kd;
        return this;
    }

    /***
     * get the factor d
     * @return d
     */
    public Double3 getkD() {
        return kD;
    }

    /***
     * set the factor d
     * @param kS object double
     * @return this
     */
    public Material setkS(double kS) {
        this.kS = new Double3(kS);
        return this;
    }

    /***
     * set the factor d
     * @param ks object Double3
     * @return this
     */
    public Material setkS(Double3 ks) {
        this.kS = ks;
        return this;
    }

    /***
     * get the factor s
     * @return s
     */
    public Double3 getkS() {
        return kS;
    }

    /**
     * set Shininess
     * @param shininess The level of shininess of the material
     * @return this
     */
    public Material setShininess(int shininess) {
        this.nShininess = shininess;
        return this;
    }

    /**
     * @return Shininess
     */
    public int getShininess() {
        return nShininess;
    }
}
