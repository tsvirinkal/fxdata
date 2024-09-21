package com.vts.fxdata.repositories;

import com.vts.fxdata.entities.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository ClientRepository) {
        this.clientRepository = ClientRepository;
    }

    public List<Client> getClients()
    {
        return this.clientRepository.findAll();
    }

    public void addClient(Client client)
    {
        this.clientRepository.saveAndFlush(client);
    }

}
