package br.com.qm.gerente.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import br.com.qm.gerente.entity.Cliente;

public class ClienteDAO {

//	O gerente é capaz de cadastrar novos clientes, 
//	remover clientes de sua carteira pelo número de conta, 
//	aumentar e diminuir o limite do cheque especial do cliente, 
//	fazer transferências entre seus clientes 
//  e adicionar saldo a um cliente, 
//	imprimir um relatório com todos os seus clientes (em um arquivo), 
//	consultar cliente pelo cpf ou cnpj, consultar cliente pela chave;
	private EntityManager entityManager;

	public ClienteDAO() {
		this.entityManager = Persistence.createEntityManagerFactory("conta_bancaria").createEntityManager();
	}

	public boolean cadastraCliente(Cliente cliente) {

		entityManager.getTransaction().begin();
		entityManager.persist(cliente);
		entityManager.getTransaction().commit();

		return true;
	}

	public boolean removeCliente(int nroConta) {

		Cliente cliente = entityManager.find(Cliente.class, nroConta);

		if (cliente == null) {
			return false;
		}

		entityManager.getTransaction().begin();
		entityManager.remove(cliente);
		entityManager.getTransaction().commit();

		return true;

	}

	public Cliente consultaCliente(int nroConta) {

		return entityManager.find(Cliente.class, nroConta);

	}

	public boolean alteraLimite(float valorAlteracao, int nroConta) {

		Cliente clienteAlterado = entityManager.find(Cliente.class, nroConta);

		if (clienteAlterado == null) {
			return false;
		}

		float valorAlterado = clienteAlterado.getLimiteChequeEspecial() + valorAlteracao;

		if (valorAlteracao > 0) {
			clienteAlterado.setLimiteChequeEspecial(valorAlterado);
		} else {
			clienteAlterado.setLimiteChequeEspecial(0F);
		}

		entityManager.getTransaction().begin();
		entityManager.merge(clienteAlterado);
		entityManager.getTransaction().commit();

		return true;
	}

	public boolean transfereValor(int nroContaOrigem, int nroContaDestino, float valorTransferido) {

		if (valorTransferido <= 0) {
			// Valor não pode ser negativo ou zero
			return false;
		}

		Cliente clienteOrigem = entityManager.find(Cliente.class, nroContaOrigem);
		Cliente clienteDestino = entityManager.find(Cliente.class, nroContaDestino);

		if (clienteOrigem == null || clienteDestino == null) {
			// Um dos clientes não foi encontrado.
			return false;
		}

		if (clienteOrigem.getSaldo() < valorTransferido) {
			// Cliente origem não tem saldo
			return false;
		}

		clienteOrigem.setSaldo(clienteOrigem.getSaldo() - valorTransferido);
		clienteDestino.setSaldo(clienteDestino.getSaldo() + valorTransferido);

		entityManager.getTransaction().begin();
		entityManager.merge(clienteOrigem);
		entityManager.merge(clienteDestino);
		entityManager.getTransaction().commit();

		return true;
	}

	public boolean adicionaSaldo(int nroConta, float valorAdicionado) {

		if (valorAdicionado <= 0) {
			return false;
		}

		Cliente cliente = entityManager.find(Cliente.class, nroConta);

		if (cliente == null) {
			return false;
		}

		cliente.setSaldo(cliente.getSaldo() + valorAdicionado);

		entityManager.getTransaction().begin();
		entityManager.merge(cliente);
		entityManager.getTransaction().commit();

		return true;
	}

	public List<Cliente> listaClientes() {

		Query query = entityManager.createQuery("select c from Cliente as c");

		return query.getResultList();
	}

	public Cliente consultaPorCpfCnpj(String cpfCnpj) {

		Query query;

		if (cpfCnpj.length() == 11) {
			query = entityManager.createQuery("select c from ClientePF as c where c.cpf = :cpf");
			query.setParameter("cpf", cpfCnpj);
		} else {
			query = entityManager.createQuery("select c from ClientePJ as c where c.cnpj = :cnpj");
			query.setParameter("cnpj", cpfCnpj);
		}

		List<Cliente> listaRetornada = query.getResultList();
		
		if (listaRetornada.size() <= 0) {
			return null;
		}
		
		return (Cliente) listaRetornada.get(0);
	}
	
	public Cliente consultaPorCpf(String cpf) {
		Query query = entityManager.createQuery("select c from ClientePF as c where c.cpf = :cpf");
		query.setParameter("cpf", cpf);
		
		List<Cliente> listaRetornada = query.getResultList();
		
		if (listaRetornada.size() <= 0) {
			return null;
		}
		
		return (Cliente) listaRetornada.get(0);
	}
	// teria que fazer outro pro PJ.

}
