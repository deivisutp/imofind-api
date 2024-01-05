package br.com.deivisutp.imofindapi;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.factory.Handler;
import br.com.deivisutp.imofindapi.factory.HandlerFactory;
import br.com.deivisutp.imofindapi.util.Address;
import br.com.deivisutp.imofindapi.util.GenericBuilder;
import br.com.deivisutp.imofindapi.util.ImovelDTOBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

class ImoFindApiApplicationTests {

	@Test
	void contextLoads() {

	}

	@Test
	void testingGenericBuilder() {
		ImovelDTO imovel = GenericBuilder.of(ImovelDTO::new)
							.with(ImovelDTO::setCity,"Blumenau")
							.with(ImovelDTO::setTitulo, "Vendo ape")
							.with(ImovelDTO::setId, 1L)
							.with(ImovelDTO::setPrice, 560000.00F)
							.build();

		System.out.println(imovel);
		assert(imovel != null && !imovel.getCity().isEmpty() && imovel.getCity().equalsIgnoreCase("Blumenau"));
	}

	@Test
	void testAnotherBuilder() {
		ImovelDTO imovelDTO = new ImovelDTOBuilder()
								.with(data -> {
									data.setId(1L);
									data.setPrice(400000F);
									data.setTitulo("VEndo");
								})
								.createImovelDTO();
		assert (imovelDTO.getId() == 1L);
	}

	@Test
	void testAddress() {
		Address address = new Address.AddressBuilder()
								.state("SC")
								.bairro("Itoupava Central")
								.zipCode("5454545")
								.country("BRasil")
								.city("Blumenau")
								.build();

		assert (address.getCity().equalsIgnoreCase("Blumenau"));
	}

	@Test
	void testingFactoryMethod() {
		HandlerFactory factory = new HandlerFactory();
		Handler<String> stringHandler = factory.getHandler(String.class);
		Handler<Date> dateHandler = factory.getHandler(Date.class);

		stringHandler.handle("Hello");
		dateHandler.handle(new Date());
	}
}
