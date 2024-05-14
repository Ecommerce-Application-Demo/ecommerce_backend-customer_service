package com.ecommerce.customer.service.impl;

import com.ecommerce.customer.dto.CustomerDto;
import com.ecommerce.customer.exception.CustomerException;
import com.ecommerce.customer.repository.CustomerAuthRepository;
import com.ecommerce.customer.repository.CustomerRepository;
import com.ecommerce.customer.service.declaration.CustomerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.ecommerce.customer.entity.CustomerAuth;
import com.ecommerce.customer.entity.Customer;

import java.util.UUID;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

	@Value("${PASSWORD.UPDATE.SUCCESS}")
	String passwordSuccessMessage;

	@Autowired
    CustomerRepository customerRepository;
	@Autowired
    CustomerAuthRepository customerAuthRepository;
	@Autowired
	ModelMapper modelMapper;
	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public void registerNewCustomer(CustomerDto customerDto) throws CustomerException {

		boolean registeredEmail =customerRepository.findByEmail(customerDto.getEmail()).isEmpty();

		if (registeredEmail) {
			Customer customer = modelMapper.map(customerDto, Customer.class);
			customer.setEmail(customer.getEmail().toLowerCase());
			customerRepository.save(customer);
			CustomerAuth customerAuth = new CustomerAuth();
			customerAuth.setEmail(customer.getEmail().toLowerCase());
			customerAuth.setPassword(passwordEncoder.encode(customer.getPassword()));
			customerAuth.setLoginSalt(UUID.randomUUID().toString().replace("-",""));
			customerAuth.setAuthCustomer(customer);
			customerAuthRepository.save(customerAuth);
		} else {
			throw new CustomerException("EMAIL.ALREADY.EXISTS", HttpStatus.UNPROCESSABLE_ENTITY);
		}

	}

	@Override
	public String welcomeService(String email) {
		Customer customer = customerRepository.findByEmail(email.toLowerCase()).orElseThrow();
		return customer.getName();
	}

	@Override
	public Boolean isPresent(String email) {
		return customerRepository.findByEmail(email.toLowerCase()).isPresent();
	}

	@Override
	public String forgetpassword(String email, String password) throws CustomerException {
		try {
			CustomerAuth customer = customerAuthRepository.findById(email.toLowerCase()).get();
			customer.setPassword(passwordEncoder.encode(password));
			customerAuthRepository.save(customer);
			customerAuthRepository.invalidateTokens(email.toLowerCase(), UUID.randomUUID().toString().replace("-", ""));
			return passwordSuccessMessage;
		} catch (Exception e) {
			throw new CustomerException("PASSWORD.UPDATE.ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Profile(value = "dev")
	@Scheduled(fixedDelay = 1000*60*5)
	 void renderRunner() {
		RestTemplate restTemplate= new RestTemplate();
		restTemplate.getForEntity("https://ecommerce-backend-customer-service.onrender.com/user/api/auth/index",String.class);
	}
}
