package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getReportingStructure(String id) {
        LOG.debug("Getting reporting structure for id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        Queue<Employee> queue = new LinkedList<>();
        queue.add(employee);

        int numberOfReports = 0;

        while(!queue.isEmpty()){
            Employee currentEmployee = queue.poll();
            List<Employee> children = currentEmployee.getDirectReports();

            if(children != null) {
                numberOfReports += children.size();

                for (int i = 0; i < children.size(); i++) {
                    Employee oneChild = children.get(i);
                    String childId = oneChild.getEmployeeId();

                    // Get the actual child object since current children are just ids.
                    Employee getActualChild = employeeRepository.findByEmployeeId(childId);

                    queue.add(getActualChild);
                }
            }
        }

        ReportingStructure reportingStructure =  new ReportingStructure(employee, numberOfReports);

        return reportingStructure;
    }

    @Override
    public Compensation createCompensation(Compensation compensation) {

        LOG.debug("Creating compensation [{}]", compensation);

        Employee employee = compensation.getEmployee();
        if (employee == null) {
            throw new RuntimeException("Employee needs to be provided to set compensation.");
        }

        String id = employee.getEmployeeId();

        Employee anEmployee = employeeRepository.findByEmployeeId(id);


        if (anEmployee == null) {
            throw new RuntimeException("Cannot create compensation for invalid employeeId: " + id);
        }

        compensationRepository.insert(compensation);

        return compensation;

    }

    @Override
    public Compensation getCompensation(String id) {
        LOG.debug("Getting compensation for id [{}]", id);

        Compensation compensation = compensationRepository.findByEmployeeId(id);

        if (compensation == null) {
            throw new RuntimeException("No compensation exists for employee with Id: " + id);
        }

        return compensation;
    }
}
