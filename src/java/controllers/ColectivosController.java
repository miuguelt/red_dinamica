package controllers;

import clases.Archivos;
import clases.Colectivos;
import clases.Formaparte;
import clases.Foros;
import controllers.util.JsfUtil;
import controllers.util.PaginationHelper;
import facade.ColectivosFacade;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("colectivosController")
@SessionScoped
public class ColectivosController implements Serializable {

    private DataModel items = null;
    @EJB
    private facade.ColectivosFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public ColectivosController() {
    }

    public Colectivos getSelected() {
        if (current == null) {
            current = new Colectivos();
            selectedItemIndex = -1;
        }
        return current;
    }

    private ColectivosFacade getFacade() {
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
        current = (Colectivos) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareEdit() {
        current = (Colectivos) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ColectivosUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Colectivos) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ColectivosDeleted"));
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

    public Colectivos getColectivos(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Colectivos.class)
    public static class ColectivosControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ColectivosController controller = (ColectivosController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "colectivosController");
            return controller.getColectivos(getKey(value));
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
            if (object instanceof Colectivos) {
                Colectivos o = (Colectivos) object;
                return getStringKey(o.getColectId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Colectivos.class.getName());
            }
        }

    }

    //CODIGO PERSONAL
    private static Colectivos current;
    private static Colectivos colectivoActual;
    private DataModel itemsAdministrador = null;
    private DataModel itemsColaborador = null;
    private List<Colectivos> colectivosColaboradorList = new ArrayList<>();
    private List<Colectivos> colectivosAllList;
    private List<Colectivos> colectivosAdminList;

    @PostConstruct
    public void init(){
        setColectivosAllList(getFacade().findAll());
        setcolectivosColaboradorList();
        setColectivosAdminList(new ArrayList<>(UsuariosController.getCurrent().getColectivosCollection1()));
        AsignarEstadoAll();
    }

    public static Colectivos getColectivoActual() {
        return colectivoActual;
    }

    public void setColectivoActual(Colectivos colectivoActual) {
        ColectivosController.colectivoActual = colectivoActual;
    }
 
   public void setColectivosAdminList(List<Colectivos> colectivosAdminList) {
        this.colectivosAdminList = colectivosAdminList;
    }

    public void setColectivosAllList(List<Colectivos> colectivos) {
        this.colectivosAllList = colectivos;
    }

    public void setColectivosColaboradorList(List<Colectivos> colectivosColaboradorList) {
        this.colectivosColaboradorList = colectivosColaboradorList;
    }

    public void setcolectivosColaboradorList() {
        List<Formaparte> formaparteColaboradorList = new ArrayList<>(UsuariosController.getCurrent().getFormaparteCollection());
        for (int i = 0; i < formaparteColaboradorList.size(); i++) {
            colectivosColaboradorList.add(formaparteColaboradorList.get(i).getColectivos());
        }
    }

    public DataModel getItemsAdministrador() {
        return itemsAdministrador = new ListDataModel(colectivosAdminList);
    }

    public DataModel getItemsColaborador() {
        return itemsColaborador = new ListDataModel(colectivosColaboradorList);
    }

    public DataModel getItems() {
//        if (items == null) {
//            items = getPagination().createPageDataModel();
//        }
        return items = new ListDataModel(colectivosAllList);
    }

    public static Colectivos getCurrent() {
        return current;
    }

    public static void setCurrent(Colectivos current) {
        ColectivosController.current = current;
    }

    public void setColectivo(Colectivos colectivo) throws IOException {
        setColectivoActual(colectivo);
        ForosController.setHayForo(false);
        JsfUtil.addSuccessMessage("Segundo mensaje");
        ComentariosController.setForoActual(new Foros());
        FacesContext.getCurrentInstance().getExternalContext().redirect("/red_dinamica/faces/web/foros/index.xhtml");
    }

    public void prepareCreate() {
        current = new Colectivos();
        selectedItemIndex = -1;
    }

    public void create() {
        try {
            asignarTodo();
            getFacade().create(current);
            recreateModel();
            colectivosAllList.add(current);
            items = new ListDataModel(colectivosAllList);
            UsuariosController.getCurrent().getColectivosCollection1().add(current);
            prepareCreate();
            //FacesContext.getCurrentInstance().getExternalContext().redirect("/red_dinamica/faces/web/foros/List.xhtml");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    public void asignarTodo() {
        current.setColectFecha(new Date());
        current.setColectUsrId(UsuariosController.getCurrent());
    }

    public void AsignarEstadoAll() {
        for (int i = 0; i < colectivosColaboradorList.size(); i++) {
            colectivosAllList.get(colectivosAllList.indexOf(colectivosColaboradorList.get(i))).setColectEstado(Boolean.TRUE);
        }
        for (int i = 0; i < colectivosAdminList.size(); i++) {
            colectivosAllList.get(colectivosAllList.indexOf(colectivosAdminList.get(i))).setColectEstado(Boolean.TRUE);
        }
        items = new ListDataModel(colectivosAllList);
    }

    public void AsignarEstado(Colectivos colectivo) {
        setCurrent(colectivo);
        current.setColectEstado(true);
        colectivosAllList.get(colectivosAllList.indexOf(current)).setColectEstado(true);
        colectivosColaboradorList.add(current);

    }
}
