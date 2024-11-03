package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.Confirmation;
import com.vts.fxdata.entities.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConfirmationService {

    private final ConfirmationRepository confirmationRepository;

    @Autowired
    public ConfirmationService(ConfirmationRepository confirmationRepository) {
        this.confirmationRepository = confirmationRepository;
    }

    public List<Confirmation> getPendingConfirmations(String pair)
    {
        return this.confirmationRepository.getPendingConfirmations(pair);
    }

    public void deleteOppositePending(Record record)
    {
        this.confirmationRepository.getPendingConfirmations(record.getPair()).stream().filter(
            c -> c.getPair()==record.getPair() && c.getTimeframe()==record.getTimeframe()
            && c.getAction()!=record.getAction()).forEach(c -> {
            this.confirmationRepository.deleteById(c.getId());
        });
    }

    public void save(Confirmation confirmation)
    {
        this.confirmationRepository.save(confirmation);
    }

    public Confirmation findById(long id)
    {
        var rec= this.confirmationRepository.findById(id);
        return rec.isPresent() ? rec.get() : null;
    }

    @Transactional
    public void deleteConfirmation(long id)
    {
        this.confirmationRepository.deleteById(id);
    }
}
