package com.ecommerce.customer.entity;

import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="Customer")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int userId;
	private String email;
	private String name;
	@Transient
	private String password;
	private String phoneNumber;
	private String gender;
	@OneToMany(mappedBy = "addCustomer" ,cascade = CascadeType.ALL)
	private List<Address> address;
	@OneToOne(mappedBy = "authCustomer",cascade = CascadeType.ALL)
	private CustomerAuth customerAuth;
	

}
