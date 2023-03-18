package rest;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.Company;

@Path("companies")
public class CompaniesResource {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Company> get() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPATestProj");
		EntityManager em = emf.createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Company> query = cb.createQuery(Company.class);
			TypedQuery<Company> tq = em.createQuery(query);
			return tq.getResultList();
		} finally {
			em.close();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Company post(@FormParam("name") String name) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPATestProj");
		EntityManager em = emf.createEntityManager();
		EntityTransaction t = em.getTransaction();
		t.begin();
		try {
			Company company = new Company();
			company.setName(name);
			em.persist(company);
			return company;
		} finally {
			t.commit();
			em.close();
		}
	}
}
