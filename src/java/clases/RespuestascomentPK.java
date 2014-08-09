/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clases;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author entorno
 */
@Embeddable
public class RespuestascomentPK implements Serializable {
    @Basic(optional = false)
    @Column(name = "resp_respuesta_id")
    private int respRespuestaId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "resp_coment_id")
    private int respComentId;

    public RespuestascomentPK() {
    }

    public RespuestascomentPK(int respRespuestaId, int respComentId) {
        this.respRespuestaId = respRespuestaId;
        this.respComentId = respComentId;
    }

    public int getRespRespuestaId() {
        return respRespuestaId;
    }

    public void setRespRespuestaId(int respRespuestaId) {
        this.respRespuestaId = respRespuestaId;
    }

    public int getRespComentId() {
        return respComentId;
    }

    public void setRespComentId(int respComentId) {
        this.respComentId = respComentId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) respRespuestaId;
        hash += (int) respComentId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RespuestascomentPK)) {
            return false;
        }
        RespuestascomentPK other = (RespuestascomentPK) object;
        if (this.respRespuestaId != other.respRespuestaId) {
            return false;
        }
        if (this.respComentId != other.respComentId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clases.RespuestascomentPK[ respRespuestaId=" + respRespuestaId + ", respComentId=" + respComentId + " ]";
    }
    
}
