package service;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import entity.Pet;

@Stateless
public class PetService {

	@PersistenceContext
	private EntityManager em;

	public List<Pet> findAll() {
		return em.createQuery("SELECT p FROM Pet p", Pet.class).getResultList();
	}

	public List<Pet> findByName(String name) {
		return em.createQuery("SELECT p FROM Pet p WHERE p.name LIKE :name", Pet.class)
				.setParameter("name", "%" + name + "%").getResultList();
	}

	public void save(Pet pet) {
		if (pet.getId() == null) {
			em.persist(pet);
		} else {
			em.merge(pet);
		}
	}

}
