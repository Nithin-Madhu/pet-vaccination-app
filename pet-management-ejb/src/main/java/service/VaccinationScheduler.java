package service;

import java.time.LocalDateTime;
import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class VaccinationScheduler {
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private JMSContext context;
    
    @Inject
    @javax.jms.JMSConnectionFactory("java:/ConnectionFactory")
    private Queue queue;
    
    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void checkVaccinations() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Object[]> results = em.createQuery(
            "SELECT p.name, o.telephone FROM Pet p JOIN p.owner o " +
            "JOIN p.vaccines v WHERE v.vaccinationDate < :time", Object[].class)
            .setParameter("time", oneHourAgo)
            .getResultList();
            
        for (Object[] result : results) {
            String message = "Pet " + result[0] + " needs vaccination. Owner contact: " + result[1];
            context.createProducer().send(queue, message);
        }
    }
}
