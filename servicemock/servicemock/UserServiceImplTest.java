package drdo.cair.isrd.icrs.mcs.services;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.datastax.driver.core.utils.UUIDs;

import drdo.cair.isrd.icrs.mcs.dao.UserDao;
import drdo.cair.isrd.icrs.mcs.entity.User;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {
	@InjectMocks
	UserService userService = new UserServiceImpl();

	@Mock
	UserDao userDao;

	private UUID uuid;
	private User user;
	private List<User> userList;

	@Before
	public void setUp() throws Exception {
		uuid = UUIDs.random();
		user = new User();
		user.setId(uuid);
		user.setName("Test");
		user.setPassword("test123");
		user.setRole(Arrays.asList("ADMIN,USER".split(",")));
		user.setRoleNames(Arrays.asList("ADMIN,USER".split(",")));
		user.setSalt(23652613);
		user.setUsername("test");
		userList = new ArrayList<User>();
		userList.add(user);
	}

	@Test
	public void createUserTest() {
		Mockito.when(userDao.createUser(user)).thenReturn(user);
		assertEquals(user, userService.createUser(user));
	}

	@Test
	public void getUserTest() {
		Mockito.when(userDao.getUser(user.getId())).thenReturn(user);
		assertEquals(user, userService.getUser(user.getId()));
	}

	@Test
	public void deleteUserTest() {
		userService.deleteUser(user.getId());
	}

	@Test
	public void updateUserTest() {
		Mockito.when(userDao.updateUser(user)).thenReturn(user);
		assertEquals(user, userService.updateUser(user));
	}

	@Test
	public void getAllUserTest() {
		Mockito.when(userDao.getAllUser()).thenReturn(userList);
		assertEquals(userList, userService.getAllUser());
	}

	@Test
	public void findByUserNameTest() {
		Mockito.when(userDao.findByUserName(user.getUsername())).thenReturn(user);
		assertEquals(user, userService.findByUserName(user.getUsername()));
	}

	@Test
	public void findAllUserNamesTest() {
		List<String> userNameList = Arrays.asList("USER1,USER2,USER3".split(","));
		Mockito.when(userDao.findAllUserNames()).thenReturn(userNameList);
		assertEquals(userNameList, userService.findAllUserNames());
	}

	@Test
	public void findUsersByRoleIdTest() {
		Mockito.when(userDao.findUsersByRoleId(user.getName())).thenReturn(userList);
		assertEquals(userList, userService.findUsersByRoleId(user.getName()));
	}
}
