package br.com.deivisutp.imofindapi;

import br.com.deivisutp.imofindapi.dto.ImovelDTO;
import br.com.deivisutp.imofindapi.factory.Handler;
import br.com.deivisutp.imofindapi.factory.HandlerFactory;
import br.com.deivisutp.imofindapi.util.Address;
import br.com.deivisutp.imofindapi.util.GenericBuilder;
import br.com.deivisutp.imofindapi.util.ImovelDTOBuilder;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;

class ImoFindApiApplicationTests {

	@Mock
	List<ImovelDTO> imoveis;

	@Spy
	Util mockedUtil;

	@Test
	void contextLoads() {

	}

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
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

	@Test
	void shouldCityMockTest() {
		ImovelDTO imovelMock = mock(ImovelDTO.class);
		when(imovelMock.getCity())
				.thenReturn("Blumenau");

		assert imovelMock.getCity().equalsIgnoreCase("Blumenau");
	}

	@Test
	void shoulListImoveisTest() {
		when(imoveis.get(0)).thenReturn(new ImovelDTOBuilder()
											.with(data -> {
												data.setId(1L);
												data.setPrice(400000F);
												data.setTitulo("Vendo");
											})
											.createImovelDTO());
		when(imoveis.size()).thenReturn(1);

		assert(imoveis.get(0).getTitulo().equalsIgnoreCase("Vendo"));
		assert(imoveis.size() == 1);
	}

	@Test
	void shouldSpyImoveisTest() {
		when(mockedUtil.process(1,1)).thenReturn(5);

		assertEquals(5, mockedUtil.process(1,1));
		System.out.println(mockedUtil.process(1,1));

		assertEquals(20, mockedUtil.process(19,1));
		System.out.println(mockedUtil.process(19,1));
	}

	class Util {
		public int process(int x, int y) {
			System.out.println("Params %x and %y" + x +  y);
			return x+y;
		}
	}
}
