package com.monstarbill.configs.feingclients;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.monstarbill.configs.models.CustomRoles;
import com.monstarbill.configs.models.DefaultRolePermissions;
import com.monstarbill.configs.models.Employee;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(name = "masters-ws")
public interface MasterServiceClient {

	@GetMapping("/masters/status/comm")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getStatusListFallback")
	public List<String> getStatusList(@RequestParam("id") int id, @RequestParam("name") String name);

	default List<String> getStatusListFallback(int id, String name, Throwable exception) {
		System.out.println("ID : " + id);
		System.out.println("Name : " + name);
		System.out.println("Exception : " + exception.getLocalizedMessage());
		return new ArrayList<String>();
	}

	@GetMapping("/employee/get-emp-id-by-mail")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getEmployeeIdByAccessMailFallback")
	public Long getEmployeeIdByAccessMail(@RequestParam("email") String email);

	default Long getEmployeeIdByAccessMailFallback(String email, Throwable exception) {
		System.out.println("Access email :: " + email);
		System.out.println("Exception : " + exception.getLocalizedMessage());
		return null;
	}

	@GetMapping("/supplier/get-supplier-id-by-mail")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "getSupplierIdByAccessMailFallback")
	public Long getSupplierIdByAccessMail(@RequestParam("email") String email);

	default Long getSupplierIdByAccessMailFallback(String email, Throwable exception) {
		System.out.println("Access email :: " + email);
		System.out.println("Exception : " + exception.getLocalizedMessage());
		return null;
	}

	@GetMapping("/roles/get-access-point-by-selected-access")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "findAccessPointBySelectedAccessFallback")
	public List<DefaultRolePermissions> findAccessPointBySelectedAccess(@RequestParam("selectedAccess") String selectedAccess);

	default List<DefaultRolePermissions> findAccessPointBySelectedAccessFallback(String selectedAccess, Throwable exception) {
		System.out.println("Access email :: " + selectedAccess);
		System.out.println("Exception : " + exception.getLocalizedMessage());
		return null;
	}

	@PostMapping("/roles/save")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "saveCustomRoleFallback")
	public CustomRoles saveCustomRole(@RequestBody CustomRoles customRole);

	default CustomRoles saveCustomRoleFallback(CustomRoles customRole, Throwable exception) {
		System.out.println("Error while saving the default role by account id :: " + customRole);
		System.out.println("Exception : " + exception.getLocalizedMessage());
		return null;
	}
	
	@PostMapping("/employee/save")
	@Retry(name = "masters-ws")
	@CircuitBreaker(name = "masters-ws", fallbackMethod = "saveEmployeeFallback")
	public Employee saveEmployee(@RequestBody Employee employee);

	default Employee saveEmployeeFallback(Employee employee, Throwable exception) {
		System.out.println("Error while saving the default Employee by account id :: " + employee);
		System.out.println("Exception : " + exception.getLocalizedMessage());
		return null;
	}

}
