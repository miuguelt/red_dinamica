package controllers;

import clases.Conversacion;
import clases.Mensaje;
import clases.Usuarios;
import static controllers.MensajeController.mensaje_List;
import controllers.util.JsfUtil;
import controllers.util.PaginationHelper;
import facade.ConversacionFacade;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@Named("conversacionController")
@SessionScoped
public class ConversacionController implements Serializable {

    private static Conversacion current;
    private DataModel items = null;
    @EJB
    private facade.ConversacionFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    public ConversacionController() {
    }

    public Conversacion getSelected() {
        if (current == null) {
            current = new Conversacion();
            selectedItemIndex = -1;
        }
        return current;
    }

    private ConversacionFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Conversacion) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Conversacion();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ConversacionCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Conversacion) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ConversacionUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Conversacion) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ConversacionDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Conversacion getConversacion(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Conversacion.class)
    public static class ConversacionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ConversacionController controller = (ConversacionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "conversacionController");
            return controller.getConversacion(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Conversacion) {
                Conversacion o = (Conversacion) object;
                return getStringKey(o.getConvId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Conversacion.class.getName());
            }
        }

    }
  
    //CODIGO PERSONAL
    private String textoMsj;
    private Mensaje nuevo;
    private Usuarios usrDestino;
    private String ms;
    private Conversacion conv_select;
    private boolean existe_conv = false;
    private int cont_msj = 0;
    private List<Mensaje> nuevos;
    private Usuarios usrActual;// = UsuariosController.getCurrent();
    public static Conversacion getCurrent() {
        return current;
    }

    public void setCurrent(Conversacion current) {
        ConversacionController.current = current;
    }

    
    public void setUsrActual(Usuarios usrActual) {
        this.usrActual = usrActual;
    }    
    public void setTextoMsj(String textoMsj) {
        this.textoMsj = textoMsj;
    }

    public void setNuevo(Mensaje nuevo) {
        this.nuevo = nuevo;
    }

    public void setMs(String ms) {
        this.ms = ms;
    }

    public void setConv_select(Conversacion conv_select) {        
        this.conv_select = conv_select;
    }

    public void setExiste_conv(boolean existe_conv) {
        this.existe_conv = existe_conv;
    }

    public void setCont_msj(int cont_msj) {
        this.cont_msj = cont_msj;
    }

    public String getTextoMsj() {
        return textoMsj;
    }

    public Mensaje getNuevo() {
        return nuevo;
    }

    public Usuarios getUsrDestino() {
        return usrDestino;
    }

    public String getMs() {
        return ms;
    }

    public Conversacion getConv_select() {
        return conv_select;
    }

    public boolean isExiste_conv() {
        return existe_conv;
    }

    public int getCont_msj() {
        return cont_msj;
    }

    public List<Mensaje> getNuevos() {
        return nuevos;
    }

    public Usuarios getUsrActual() {
        return usrActual;
    }
    
    public void leerMensajes() {
        JsfUtil.addSuccessMessage("marca1");
        List<Mensaje> msjs = (List<Mensaje>) current.getMensajeCollection();
        for (Mensaje mensaje : msjs) {
            if (!mensaje.getMsjLeido() && mensaje.getMsjDestinatario().equals(getUsrActual())) {
                cont_msj--;
                mensaje.setMsjLeido(true);
            }
        }

        current.setMensajeCollection(msjs);
        update();
    }

//    public void cargarConversaciones() {
//        try {
//            if (ejbFacade.cargarConversaciones(getUsrActual().getUsrId()).isEmpty()) {
//                List<Conversacion> lista_conv = ejbFacade.cargarConversaciones(getUsrActual().getUsrId());
//                HashSet<Usuarios> hashSet = new HashSet(lista_conv);
//                lista_conv = new ArrayList<>();
//                // Eliminamos Usuarios repetidos
//                for (Iterator it = hashSet.iterator(); it.hasNext();) {
//                    lista_conv.add((Conversacion) it.next());
//                }
//                items = new ListDataModel(lista_conv);
//            }
//        } catch (Exception e) {
//            JsfUtil.addErrorMessage("Error al cargar las conversaciones del usuario... " + e);
//        }
//    }

    public void cargarConversacion() throws IOException {
        try {
            recreateModel();
            List<Conversacion> listaConversacion = ejbFacade.existeConversacion(getUsrActual().getUsrId(), getUsrDestino().getUsrId());
            if (!listaConversacion.isEmpty()) { //Si la conversacion existe
                JsfUtil.addSuccessMessage("Tamaño: " + listaConversacion.size());
                JsfUtil.addSuccessMessage("0: " + listaConversacion.get(0).getConvUsr2Id().getUsrNombres());
                setCurrent(listaConversacion.get(0));
                JsfUtil.addSuccessMessage("Current: "+ getCurrent().getConvUsr2Id().getUsrNombres());
                if(!getCurrent().getMensajeCollection().isEmpty()){ //Si hay mensages
                    JsfUtil.addSuccessMessage("Coleccion vacia:"+getCurrent().getMensajeCollection().isEmpty());
                    MensajeController.setMensaje_List((List<Mensaje>) getCurrent().getMensajeCollection());
                    JsfUtil.addSuccessMessage("Mensage List vacio:"+MensajeController.getMensaje_List().isEmpty());       
                }else{ //Si no hay mensages
                    JsfUtil.addSuccessMessage("Mensaje list null");
                    MensajeController.setMensaje_List(null);
                }
            } else { //Si la conversacion no existe
                MensajeController.setMensaje_List(null);
                JsfUtil.addSuccessMessage("Mensaje list null");
                JsfUtil.addSuccessMessage("Está vacia"); 
                current = new Conversacion();
                current.setConvAsunto(" ");
                current.setConvUltimo(new Date());
                current.setConvNumero(0);
                current.setConvUsr1Id(getUsrActual());
                current.setConvUsr2Id(getUsrDestino());
                getFacade().create(current);
                JsfUtil.addSuccessMessage("Conversación nueva creada");
                recreateModel();
                //setExiste_conv(false);
                
            }
//            HashSet<Usuarios> hashSet = new HashSet(lista_conv);
//            lista_conv = new ArrayList<>();
//            //Eliminamos Usuarios repetidos
//            for (Iterator it = hashSet.iterator(); it.hasNext();) {
//                lista_conv.add((Conversacion) it.next());
//            }
//            if (lista_conv.isEmpty()) {
//                recreateModel();
//                current = new Conversacion();
//                current.setConvAsunto(" ");
//                Date fechaActual = new Date();
//                current.setConvUltimo(fechaActual);
//                current.setConvNumero(0);
//                current.setConvUsr1Id(getUsrActual());
//                current.setConvUsr2Id(getUsrDestino());
//                getFacade().create(current);
//                JsfUtil.addSuccessMessage("Conversación nueva creada");
//                recreateModel();
//                setExiste_conv(false);
//            } else {
//                if (lista_conv.get(0).getMensajeCollection().isEmpty()) {
//                    JsfUtil.addSuccessMessage("Esta conversación esta vacia !!!");
//                    setCurrent(lista_conv.get(0));
//                    destroy();
//                    recreateModel();
//                    cargarConversacion();
//                    return;
//                } else {                    
//                    current = lista_conv.get(0);
//                    JsfUtil.addSuccessMessage("Collection null: " + current.getMensajeCollection().isEmpty());
//                    List<Mensaje> msj = (List<Mensaje>) current.getMensajeCollection();
//                    JsfUtil.addSuccessMessage("Remitente: " + msj.get(0).getMsjRemitente().getUsrNombres());
//                    //Leer mensajes si el usuario destinatario es el actual y si no es está leído el mensaje
//                    if (msj.get(msj.size()).getMsjDestinatario().getUsrId() == getUsrActual().getUsrId())//si el usuario actual es a quien le envian el msj cambiarlo a true
//                    {
//                        JsfUtil.addSuccessMessage("marca5");
//                        leerMensajes();
//                    }
//                    JsfUtil.addSuccessMessage("marca4");
//                    if (current.getConvAsunto().compareTo(" ") == 0) {
//                        setExiste_conv(false);
//                        JsfUtil.addSuccessMessage("marca3");
//                    } else {
//                        JsfUtil.addSuccessMessage("marca2");
//                        setExiste_conv(true);
//                    }
//                    JsfUtil.addSuccessMessage("La conversación existe, cargando ...");
//                }
//            }
//            setConv_select(current);
//            //cargarConversaciones();
            FacesContext.getCurrentInstance().getExternalContext().redirect("/red_dinamica/faces/web/conversacion/List.xhtml");
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error al cargar las conversaciones... : " + e + " Localize: " + e.getLocalizedMessage() + "  cause:   " + e.getCause());
        }
    }

    public void setUsrDestino(Usuarios usrDestino) throws IOException {
        try {
            JsfUtil.addSuccessMessage("Set usr Destino");
            this.usrDestino = usrDestino;
            setUsrActual(UsuariosController.getCurrent());
            JsfUtil.addSuccessMessage("Destino"+ getUsrDestino().getUsrNombres());
            cargarConversacion();
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error al asignar el usuario destino o al cargar la conversación");
        }
    }
      
    public void RowSelect(SelectEvent event) {
        try {
            JsfUtil.addSuccessMessage("Row select");
//            setConv_select((Conversacion) (event.getObject()));            
            setCurrent(getConv_select());
            this.usrDestino = getCurrent().getConvUsr2Id();
            setUsrActual(UsuariosController.getCurrent());
            JsfUtil.addSuccessMessage("Conversacion row Select: "+current.getConvUsr2Id().getUsrNombres());
//            cargarConversacion();
//            if (current.getConvUsr2Id().getUsrId() == getUsrActual().getUsrId()) {
//                setUsrDestino(current.getConvUsr1Id());
//            } else {
//                setUsrDestino(current.getConvUsr2Id());
//            }
            if (!getCurrent().getMensajeCollection().isEmpty()) { //Si la conversacion tiene mensajes
                JsfUtil.addSuccessMessage("Conversacion teiene mensajes");
                MensajeController.setMensaje_List((List<Mensaje>) getCurrent().getMensajeCollection());
            }else { //Si la conversacion No tiene mensajes
                JsfUtil.addSuccessMessage("Conversacion no tiene mensajes");
                MensajeController.setMensaje_List(null);
            }
            JsfUtil.addSuccessMessage("Colection isEmpty: "+current.getMensajeCollection().isEmpty());
            //leerMensajes();
//            JsfUtil.addSuccessMessage("Conversación seleccionada: " + ((Conversacion) event.getObject()).getConvId());
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error al seleccionar la columna");
        }
    }

    public void RowUnselect(UnselectEvent event) {
        JsfUtil.addSuccessMessage("RowUnselect");
        FacesMessage msg = new FacesMessage("Conversacion no seleccionada", "" + ((Conversacion) event.getObject()).getConvId());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void enviarMsj() {
        try {
            setExiste_conv(true);
            Date fechaActual = new Date();
            Mensaje mensaje = new Mensaje();
            mensaje.setMsjTexto(textoMsj);
            mensaje.setMsjConversacion(current);
            mensaje.setMsjFecha(fechaActual);
            mensaje.setMsjLeido(false);
            mensaje.setMsjRemitente(getUsrActual());
            mensaje.setMsjDestinatario(usrDestino);
            current.getMensajeCollection().add(mensaje);
            update();
            setConv_select(current);
            //JsfUtil.addSuccessMessage("Mensaje enviado!");

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Error al crear la conversación: " + e + "  Localización: " + e.getLocalizedMessage() + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

     public void buttonAction(ActionEvent actionEvent) {
        addMessage("Welcome to Primefaces!!");
        JsfUtil.addSuccessMessage("Mi Mensaje"); 
    }
    
     public void addMessage(String summary) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
     
    
}
