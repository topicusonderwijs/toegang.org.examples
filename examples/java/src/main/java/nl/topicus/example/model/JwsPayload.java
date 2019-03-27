package nl.topicus.example.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JwsPayload implements Serializable {

    // organisation
    private String org = "";

    // firstname
    private String fn = "";

    // account identifier (specific for organisation)
    private String sub = "";

    // expiration date
    private long exp = -1;

    //tlink code
    private String tlink = "";

    // ean
    private String ean = "";

    // audience
    private String aud = "";

    // referentie voor de gebruiker: meldcode
    private String ref = "";

    // linked accounts; array with accounts previously associated with account. (merged).
    private List<String> lac = new ArrayList<>();

    // random value
    private String rnd = "";

    // rol van de gebruiker
    private String rol = "";

    public String getOrg() {
        return org;
    }

    public String getFn() {
        return fn;
    }

    public String getSub() {
        return sub;
    }

    public long getExp() {
        return exp;
    }
    public String getTlink() {
        return tlink;
    }

    public String getEan() {
        return ean;
    }

    public String getAud() {
        return aud;
    }

    public String getRef() {
        return ref;
    }

    public List<String> getLac() {
        return lac;
    }

    public String getRnd() {
        return rnd;
    }

    public String getRol() {
        return rol;
    }

    /**
     * Plaats alle waarden, behalve die van de tlink en lac, in een lijst als string
     * Deze wordt gebruikt om alle velden, aan de voorkant, in te vullen met haar correcte waarde(n)
     *
     * @return de lijst met daarin de string waarde(n) van alle fields
     */
    @JsonIgnore
    public List<String> getValues(){
        return Arrays.asList(this.org, this.fn, this.sub, ""+this.exp, this.ean, this.aud, this.ref, this.rnd, this.rol);
    }
}
