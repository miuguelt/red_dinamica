/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facade;

import clases.Formaparte;
import controllers.util.JsfUtil;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author entorno
 */
@Stateless
public class FormaparteFacade extends AbstractFacade<Formaparte> {

    @PersistenceContext(unitName = "red_dinamicaPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public FormaparteFacade() {
        super(Formaparte.class);
    }

//    public void actulizarEm(Formaparte formaparte) {
//        em.merge(formaparte);
////        JsfUtil.addSuccessMessage("Entity");
//                
//        //em.flush();
//    }

}
