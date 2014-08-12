package controllers;

import clases.Conversacion;
import clases.Universidades;
import clases.Usuarios;
import controllers.util.JsfUtil;
import controllers.util.PaginationHelper;
import controllers.util.PasswordService;
import facade.UsuariosFacade;
import java.io.Serializable;
import java.util.ArrayList;
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
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

@Named("usuariosController")
@SessionScoped
public class UsuariosController implements Serializable {

    private static Usuarios current;
    private DataModel items = null;
    @EJB
    private facade.UsuariosFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public UsuariosController() {
    }

    public Usuarios getSelected() {
        if (current == null) {
            current = new Usuarios();
            selectedItemIndex = -1;
        }
        return current;
    }

    private UsuariosFacade getFacade() {
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
                    return new ListDataModel(getListaUsrs());
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
        current = (Usuarios) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Usuarios();
        selectedItemIndex = -1;
        return "Create";
    }

    public String prepareEdit() {
        current = (Usuarios) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsuariosUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Usuarios) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsuariosDeleted"));
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
        if (items == null || listaUsrs.isEmpty()) {
            items = getPagination().createPageDataModel();
        }
        return items; //= new ListDataModel(listaUsrs);
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

    public Usuarios getUsuarios(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Usuarios.class)
    public static class UsuariosControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UsuariosController controller = (UsuariosController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "usuariosController");
            return controller.getUsuarios(getKey(value));
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
            if (object instanceof Usuarios) {
                Usuarios o = (Usuarios) object;
                return getStringKey(o.getUsrId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Usuarios.class.getName());
            }
        }

    }

        //Codigo personal
    public static Usuarios getCurrent() {
        return current;
    }

    public static void setCurrent(Usuarios current) {
        UsuariosController.current = current;
    }

    public String encriptarPass(String palabra) {
        PasswordService pws = PasswordService.getInstance();
        String hash = null;
        try {
            hash = pws.encrypt(palabra);
            return hash;
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error de encriptación");
        }
        return null;
    }

    public String create() {
        try {
            current.setUsrPass(encriptarPass(current.getUsrPass()));
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsuariosCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    //Cambiar clave
    String actualPass;
    String nuevaPass;
    String nuevaPassConfir;

    public String getActualPass() {
        return actualPass;
    }

    public void setActualPass(String actualPass) {
        this.actualPass = actualPass;
    }

    public String getNuevaPass() {
        return nuevaPass;
    }

    public void setNuevaPass(String nuevaPass) {
        this.nuevaPass = nuevaPass;
    }

    public String getNuevaPassConfir() {
        return nuevaPassConfir;
    }

    public void setNuevaPassConfir(String nuevaPassConfir) {
        this.nuevaPassConfir = nuevaPassConfir;
    }

    public void validarPassActual(FacesContext arg0, UIComponent arg1, Object arg2)
            throws ValidatorException {
        String pas = encriptarPass((String) arg2);
        String actual = current.getUsrPass();
        actualPass = actual;
        if (!pas.equals(actual)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "La contraseña no es la actual ", "La contraseña no es la actual: "));

        }
    }

    public void validarNuevaConfirmar(FacesContext arg0, UIComponent arg1, Object arg2)
            throws ValidatorException {
        nuevaPassConfir = (String) arg2;

        if (!getNuevaPassConfir().equals(getNuevaPass())) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Las contraseñas no coinciden", "Las contraseñas no coinciden "));
        }
    }

    public void actualizarContraseña() {
        //Encriptamos primero la contraseña
        current.setUsrPass(encriptarPass(this.nuevaPass));
        update();
        JsfUtil.addSuccessMessage("Contraseña actualizada con éxito!");
    }

//Busca personas
    private String nombre_buscar;
    private String filtrar_por = "1";// tipo de filtro al buscar usuarios:(1=nombre, 2= email, 3= universidad)
    @EJB
    private facade.UniversidadesFacade Ufacade;
    List<Usuarios> listaUsuariosE = new ArrayList<>();
    Universidades u_select;
    private Usuarios usrSelect;
    List<Universidades> listaUs = new ArrayList<>();
    private boolean buscarU = false;
    List<Usuarios> listaUsrs = new ArrayList<>();
    List<Conversacion> conversaciones;
    List<Usuarios> contactos = new ArrayList<>();

    public List<Universidades> getListaUs() {
        return listaUs;
    }

    public void setListaUs(List<Universidades> listaUs) {
        this.listaUs = listaUs;
    }
    public String getFiltar_por() {
        return filtrar_por;
    }

    public void setFiltar_por(String filtrar_por) {
        this.filtrar_por = filtrar_por;
    }

    public boolean isBuscarU() {
        return buscarU;
    }

    public void setBuscarU(boolean buscarU) {
        this.buscarU = buscarU;
    }

    public String getNombre_buscar() {
        return nombre_buscar;
    }

    public void setNombre_buscar(String nombre_buscar) {
        this.nombre_buscar = nombre_buscar;
    }

    public Universidades getU_select() {
        return u_select;
    }

    public void setU_select(Universidades u_select) {
        this.u_select = u_select;
    }
    
    
    public List<Usuarios> getContactos() {
        contactos = new ArrayList<>();
        if (getCurrent().getUsuariosCollection() != null && !getCurrent().getUsuariosCollection().isEmpty()) {
            contactos.addAll(getCurrent().getUsuariosCollection());
        }
//        else if(getCurrent().getUsuariosCollection1()!=null && !getCurrent().getUsuariosCollection1().isEmpty())
//        contactos.addAll(getCurrent().getUsuariosCollection1());
        return contactos;
    }

    public List<Usuarios> getListaUsrs() {
        listaUsrs = ejbFacade.buscarUsuariosActivos();
        listaUsrs.remove(current);
        for (Usuarios cont : getContactos()) {
            listaUsrs.remove(cont);
        }
        return listaUsrs;
    }

    public void setListaUsrs(List<Usuarios> listaUsrs) {
        this.listaUsrs = listaUsrs;
    }

    public void asignarUsuariosEncontradosPorUniversidad() {
        try {
            listaUsuariosE = new ArrayList<>();
            Universidades U;
            for (Usuarios usr : listaUsrs) {
                U = usr.getUsrUniversidad();
                if (U != null) {
                    if (U.getUniversidadId() == u_select.getUniversidadId()) {
                        listaUsuariosE.add(usr);
                    }
                }
            }

//            listaUsuariosE = ejbFacade.buscarUsuarios_por_universidad(u_select.getUniversidadId());
//            listaUsuariosE.remove(current);
//            HashSet<Usuarios> hashSet = new HashSet(listaUsuariosE);
//            listaUsuariosE = new ArrayList<>();
//            // Eliminamos Usuarios repetidos
//            for (Iterator it = hashSet.iterator(); it.hasNext();) {
//                listaUsuariosE.add((Usuarios) it.next());
//            }
            if (listaUsuariosE.isEmpty()) {
                items = new ListDataModel(getListaUsrs());
                JsfUtil.addSuccessMessage("No hay usuarios");
            } else {
                items = new ListDataModel(listaUsuariosE);
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error al cargar los usuarios por universidad: " + e);
        }
    }

    public void asignarFiltro(FacesContext facesContext, UIComponent component, Object value) {
        try {
            buscarU = false;
            listaUsuariosE = new ArrayList<>();
            items = new ListDataModel(getListaUsrs());//pasamos la lista al datamodel
            this.filtrar_por = "" + value;
            if (filtrar_por.equals("3")) {
                listaUs = Ufacade.findAll();
                u_select = listaUs.get(0);
                buscarU = true;
                asignarUsuariosEncontradosPorUniversidad();

            }
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error filtro: " + value + " " + e + "\nLocalize: " + e.getLocalizedMessage(), "  nombre no asignado: " + e + "\nLocalize: " + e.getLocalizedMessage()));
        }
    }

    public void asignarNombreBuscar(FacesContext facesContext, UIComponent component, Object value) {
        try {
            this.nombre_buscar = value.toString();
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nombre: " + value + "Error: ", "" + e + "   " + e.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage("buscar", msg);
        }
    }

    public void asignarUsuariosEncontradosPorNombre() {
        try {//Buscamos en los usuarios encontrados
            listaUsuariosE = new ArrayList<>();
            String nombre;
            for (Usuarios usr : listaUsrs) {
                nombre = usr.getUsrNombres() + " " + usr.getUsrApellidos();
                if (nombre.toLowerCase().contains(getNombre_buscar().toLowerCase())) {
                    listaUsuariosE.add(usr);
                    //JsfUtil.addSuccessMessage("Usuario agregado! " + usr.getUsrNombres());
                }
            }

            if (listaUsuariosE.isEmpty()) {
                items = new ListDataModel(getListaUsrs());
                JsfUtil.addSuccessMessage("No hay usuarios");
            } else {
                items = new ListDataModel(listaUsuariosE);
            }

        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: " + this.nombre_buscar + "  nombre no asignado: " + e + "\nLocalize: " + e.getLocalizedMessage(), "  nombre no asignado: " + e + "\nLocalize: " + e.getLocalizedMessage()));
        }
    }

    public void asignarUsuariosEncontradosPorEmail() {
        try {
            listaUsuariosE = new ArrayList<>();
            String email = "";
            for (Usuarios usr : listaUsrs) {
                email = usr.getUsrEmail();
                if (email.toLowerCase().contains(getNombre_buscar().toLowerCase())) {
                    listaUsuariosE.add(usr);
                }
            }

            if (listaUsuariosE.isEmpty()) {
                items = new ListDataModel(getListaUsrs());
                JsfUtil.addSuccessMessage("No hay usuarios");
            } else {
                items = new ListDataModel(listaUsuariosE);
            }
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: " + this.nombre_buscar + "  nombre no asignado: " + e + "\nLocalize: " + e.getLocalizedMessage(), "  nombre no asignado: " + e + "\nLocalize: " + e.getLocalizedMessage()));
        }
    }

    public void aplicarFiltro() {
        try {
            switch (filtrar_por) {
                case "1":
                    asignarUsuariosEncontradosPorNombre();
                    break;
                case "2":
                    asignarUsuariosEncontradosPorEmail();
                    break;
            }

        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error filtro: " + e + "\nLocalize: " + e.getLocalizedMessage(), "  nombre no asignado: " + e + "\nLocalize: " + e.getLocalizedMessage()));
        }
    }

    public void aplicarFiltroUs(FacesContext facesContext, UIComponent component, Object value) {
        try {
            asignarUsuariosEncontradosPorUniversidad();
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error filtro: " + e + "\nLocalize: " + e.getLocalizedMessage(), "  nombre no asignado: " + e + "\nLocalize: " + e.getLocalizedMessage()));
        }
    }

    
}
