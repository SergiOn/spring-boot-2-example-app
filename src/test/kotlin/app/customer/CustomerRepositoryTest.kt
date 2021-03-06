package app.customer

import app.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier

class CustomerRepositoryTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var repository: CustomerRepository

    @Test
    fun findByName() {
        val customer = generateCustomer()
        repository.save(customer).block()

        StepVerifier.create(
            repository
                .findByName(customer.name)
                .collectList())
            .assertNext {
                it.isNotEmpty() &&
                    it.first { it.id == customer.id } != null
            }
            .verifyComplete()

        repository.delete(customer).block()
    }

    @Test
    fun save() {
        val customer = generateCustomer()
        repository.save(customer).block()

        StepVerifier.create(
            repository
                .findById(customer.id))
            .expectNext(customer)
            .verifyComplete()

        repository.delete(customer).block()
    }

    @Test
    fun update() {
        val customer = generateCustomer()
        repository.save(customer).block()

        val newName = "Jane Doe"
        StepVerifier.create(
            repository.save(customer.copy(name = newName))
                .flatMap {
                    repository
                        .findById(customer.id)
                        .thenReturn(it.name)
                })
            .expectNext(newName)
            .verifyComplete()

        repository.deleteById(customer.id).block()
    }

    @Test
    fun delete() {
        val customer = generateCustomer()
        repository.save(customer).block()

        StepVerifier.create(
            repository
                .delete(customer)
                .then(
                    repository
                        .findByName(customer.name)
                        .collectList()
                ))
            .assertNext { it.isEmpty() }
            .verifyComplete()
    }
}
