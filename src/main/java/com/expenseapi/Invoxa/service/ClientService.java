package com.expenseapi.Invoxa.service;

import com.expenseapi.Invoxa.dto.ClientResponse;
import com.expenseapi.Invoxa.dto.CreateClientRequest;
import com.expenseapi.Invoxa.model.Client;
import com.expenseapi.Invoxa.model.Tenant;
import com.expenseapi.Invoxa.repository.ClientRepository;
import com.expenseapi.Invoxa.repository.TenantRepository;
import com.expenseapi.Invoxa.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final TenantRepository tenantRepository;

    public ClientResponse createClient(CreateClientRequest request, AuthenticatedUser currentUser) {
        Tenant tenant = tenantRepository.findById(currentUser.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        Client client = new Client();
        client.setTenant(tenant);
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());

        client = clientRepository.save(client);
        return toResponse(client);
    }

    public List<ClientResponse> getAllClients(AuthenticatedUser currentUser) {
        return clientRepository.findByTenantId(currentUser.getTenantId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse getClient(UUID clientId, AuthenticatedUser currentUser) {
        Client client = clientRepository.findByIdAndTenantId(clientId, currentUser.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return toResponse(client);
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getCreatedAt()
        );
    }
}