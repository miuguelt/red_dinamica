/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clases;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author entorno
 */
@Entity
@Table(name = "Foros")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Foros.findAll", query = "SELECT f FROM Foros f"),
    @NamedQuery(name = "Foros.findByForoId", query = "SELECT f FROM Foros f WHERE f.foroId = :foroId"),
    @NamedQuery(name = "Foros.findByForoTema", query = "SELECT f FROM Foros f WHERE f.foroTema = :foroTema"),
    @NamedQuery(name = "Foros.findByForoFecha", query = "SELECT f FROM Foros f WHERE f.foroFecha = :foroFecha"),
    @NamedQuery(name = "Foros.findByForoDescripcion", query = "SELECT f FROM Foros f WHERE f.foroDescripcion = :foroDescripcion")})
public class Foros implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "foro_id")
    private Integer foroId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "foro_tema")
    private String foroTema;
    @Basic(optional = false)
    @NotNull
    @Column(name = "foro_fecha")
    @Temporal(TemporalType.DATE)
    private Date foroFecha;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 700)
    @Column(name = "foro_descripcion")
    private String foroDescripcion;
    @JoinColumn(name = "foro_usr_id", referencedColumnName = "usr_id")
    @ManyToOne(optional = false)
    private Usuarios foroUsrId;
    @JoinColumn(name = "foro_colect_id", referencedColumnName = "colect_id")
    @ManyToOne(optional = false)
    private Colectivos foroColectId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "comentForoId")
    private Collection<Comentarios> comentariosCollection;

    public Foros() {
    }

    public Foros(Integer foroId) {
        this.foroId = foroId;
    }

    public Foros(Integer foroId, String foroTema, Date foroFecha, String foroDescripcion) {
        this.foroId = foroId;
        this.foroTema = foroTema;
        this.foroFecha = foroFecha;
        this.foroDescripcion = foroDescripcion;
    }

    public Integer getForoId() {
        return foroId;
    }

    public void setForoId(Integer foroId) {
        this.foroId = foroId;
    }

    public String getForoTema() {
        return foroTema;
    }

    public void setForoTema(String foroTema) {
        this.foroTema = foroTema;
    }

    public Date getForoFecha() {
        return foroFecha;
    }

    public void setForoFecha(Date foroFecha) {
        this.foroFecha = foroFecha;
    }

    public String getForoDescripcion() {
        return foroDescripcion;
    }

    public void setForoDescripcion(String foroDescripcion) {
        this.foroDescripcion = foroDescripcion;
    }

    public Usuarios getForoUsrId() {
        return foroUsrId;
    }

    public void setForoUsrId(Usuarios foroUsrId) {
        this.foroUsrId = foroUsrId;
    }

    public Colectivos getForoColectId() {
        return foroColectId;
    }

    public void setForoColectId(Colectivos foroColectId) {
        this.foroColectId = foroColectId;
    }

    @XmlTransient
    public Collection<Comentarios> getComentariosCollection() {
        return comentariosCollection;
    }

    public void setComentariosCollection(Collection<Comentarios> comentariosCollection) {
        this.comentariosCollection = comentariosCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (foroId != null ? foroId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Foros)) {
            return false;
        }
        Foros other = (Foros) object;
        if ((this.foroId == null && other.foroId != null) || (this.foroId != null && !this.foroId.equals(other.foroId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "clases.Foros[ foroId=" + foroId + " ]";
    }
    
}
