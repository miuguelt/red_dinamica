/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clases;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author entorno
 */
@Entity
@Table(name = "Historial_archivos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Historialarchivos.findAll", query = "SELECT h FROM Historialarchivos h"),
    @NamedQuery(name = "Historialarchivos.findByHistorialId", query = "SELECT h FROM Historialarchivos h WHERE h.historialId = :historialId"),
    @NamedQuery(name = "Historialarchivos.findByHistorialFecha", query = "SELECT h FROM Historialarchivos h WHERE h.historialFecha = :historialFecha")})
public class Historialarchivos implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "historial_id")
    private Integer historialId;
    @Column(name = "historial_fecha")
    @Temporal(TemporalType.DATE)
    private Date historialFecha;
    @JoinColumn(name = "historial_usr_id", referencedColumnName = "usr_id")
    @ManyToOne(optional = false)
    private Usuarios historialUsrId;
    @JoinColumn(name = "historial_archivo_id", referencedColumnName = "archivo_id")
    @ManyToOne(optional = false)
    private Archivos historialArchivoId;

    public Historialarchivos() {
    }

    public Historialarchivos(Integer historialId) {
        this.historialId = historialId;
    }

    public Integer getHistorialId() {
        return historialId;
    }

    public void setHistorialId(Integer historialId) {
        this.historialId = historialId;
    }

    public Date getHistorialFecha() {
        return historialFecha;
    }

    public void setHistorialFecha(Date historialFecha) {
        this.historialFecha = historialFecha;
    }

    public Usuarios getHistorialUsrId() {
        return historialUsrId;
    }

    public void setHistorialUsrId(Usuarios historialUsrId) {
        this.historialUsrId = historialUsrId;
    }

    public Archivos getHistorialArchivoId() {
        return historialArchivoId;
    }

    public void setHistorialArchivoId(Archivos historialArchivoId) {
        this.historialArchivoId = historialArchivoId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (historialId != null ? historialId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Historialarchivos)) {
            return false;
        }
        Historialarchivos other = (Historialarchivos) object;
        if ((this.historialId == null && other.historialId != null) || (this.historialId != null && !this.historialId.equals(other.historialId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clases.Historialarchivos[ historialId=" + historialId + " ]";
    }
    
}
