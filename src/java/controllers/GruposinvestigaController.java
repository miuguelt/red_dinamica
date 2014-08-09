package controllers;

import clases.Gruposinvestiga;
import controllers.util.JsfUtil;
import controllers.util.PaginationHelper;
import facade.GruposinvestigaFacade;

import java.io.Serializable;
import java.util.ResourceBundle;
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

@Named("gruposinvestigaController")
@SessionScoped
public class GruposinvestigaController implements Serializable {

    private Gruposinvestiga current;
    private DataModel items = null;
    @EJB
    private facade.GruposinvestigaFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public GruposinvestigaController() {
    }

    public Gruposinvestiga getSelected() {
        if (current == null) {
            current = new Gruposinvestiga();
            current.setGruposinvestigaPK(new clases.GruposinvestigaPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private GruposinvestigaFacade getFacade() {
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
        current = (Gruposinvestiga) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Gruposinvestiga();
        current.setGruposinvestigaPK(new clases.GruposinvestigaPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getGruposinvestigaPK().setGruposInvestigaUniversidad(current.getUniversidades().getUniversidadId());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GruposinvestigaCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Gruposinvestiga) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getGruposinvestigaPK().setGruposInvestigaUniversidad(current.getUniversidades().getUniversidadId());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GruposinvestigaUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Gruposinvestiga) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GruposinvestigaDeleted"));
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

    public Gruposinvestiga getGruposinvestiga(clases.GruposinvestigaPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Gruposinvestiga.class)
    public static class GruposinvestigaControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            GruposinvestigaController controller = (GruposinvestigaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "gruposinvestigaController");
            return controller.getGruposinvestiga(getKey(value));
        }

        clases.GruposinvestigaPK getKey(String value) {
            clases.GruposinvestigaPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new clases.GruposinvestigaPK();
            key.setGruposInvestigaId(Integer.parseInt(values[0]));
            key.setGruposInvestigaUniversidad(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(clases.GruposinvestigaPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getGruposInvestigaId());
            sb.append(SEPARATOR);
            sb.append(value.getGruposInvestigaUniversidad());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Gruposinvestiga) {
                Gruposinvestiga o = (Gruposinvestiga) object;
                return getStringKey(o.getGruposinvestigaPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Gruposinvestiga.class.getName());
            }
        }

    }

}
