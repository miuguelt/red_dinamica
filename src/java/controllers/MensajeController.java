package controllers;

import clases.Mensaje;
import controllers.util.JsfUtil;
import controllers.util.PaginationHelper;
import facade.MensajeFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("mensajeController")
@RequestScoped
public class MensajeController implements Serializable {

    private static Mensaje current;
    private DataModel items = null;
    @EJB
    private facade.MensajeFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public MensajeController() {
    }

    public Mensaje getSelected() {
        if (current == null) {
            current = new Mensaje();
            selectedItemIndex = -1;
        }
        return current;
    }

    private MensajeFacade getFacade() {
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
        current = (Mensaje) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }


    public String prepareEdit() {
        current = (Mensaje) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MensajeUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Mensaje) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MensajeDeleted"));
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
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
        recreateModel();
        return items = new ListDataModel(mensaje_List); // = new ListDataModel((List) ConversacionController.getCurrent().getMensajeCollection()) ;
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

    public Mensaje getMensaje(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Mensaje.class)
    public static class MensajeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            MensajeController controller = (MensajeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "mensajeController");
            return controller.getMensaje(getKey(value));
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
            if (object instanceof Mensaje) {
                Mensaje o = (Mensaje) object;
                return getStringKey(o.getMsjId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Mensaje.class.getName());
            }
        }

    }
    
    //CODIGO PERSONAL
    static List<Mensaje> mensaje_List = new ArrayList<>();

     
    public static List<Mensaje> getMensaje_List() {
        return mensaje_List;
    }

    public static void setMensaje_List(List<Mensaje> mensaje_List) {
        MensajeController.mensaje_List = mensaje_List;
    }       
       
    public static Mensaje getCurrent() {
        return current;
    }

    public static void setCurrent(Mensaje current) {
        MensajeController.current = current;
    }
    
    public void prepareCreate() {
        current = new Mensaje();
        selectedItemIndex = -1;
        JsfUtil.addSuccessMessage("prepare Crate");
    }
        public void create() {
        try {
            JsfUtil.addSuccessMessage("Create");
            asignarTodo();
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MensajeCreated"));
          prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }
        
    public void asignarTodo() {
        JsfUtil.addSuccessMessage("asignar todo");
        current.setMsjFecha(new Date());
        JsfUtil.addSuccessMessage("Destinatario" + ConversacionController.getCurrent().getConvUsr2Id());
        current.setMsjDestinatario(ConversacionController.getCurrent().getConvUsr2Id());
        current.setMsjRemitente(ConversacionController.getCurrent().getConvUsr1Id());
        current.setMsjConversacion(ConversacionController.getCurrent());        
        setMensaje_List((List<Mensaje>) ConversacionController.getCurrent().getMensajeCollection());
        mensaje_List.add(current);
        items = new ListDataModel(mensaje_List);
    }
}