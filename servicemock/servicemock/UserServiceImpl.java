package drdo.cair.isrd.icrs.mcs.services;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;

import drdo.cair.isrd.icrs.mcs.entity.User;
import drdo.cair.isrd.icrs.mcs.repository.UserRepository;

/**
 * 
 * @author user
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	public UserRepository userRepo;
	
	@Autowired
	private CassandraOperations cassandraTemplate;
	
	@Autowired
	private Md5PasswordEncoder md5PasswordEncoder;

	/**
	 * User to Create User Information
	 * 
	 * @param user
	 * @return {@link UserDao}
	 */
	@Override
	public User createUser(User user) {
		// Encode the raw password with md5 hash and salt.
		SecureRandom secureRandom = new SecureRandom();
		long salt = secureRandom.nextLong();
		String encodedPassword = md5PasswordEncoder.encodePassword(user.getPassword(), salt);
		// Update the user object with salt and encoded password
		user.setSalt(salt);
		user.setPassword(encodedPassword);
		return cassandraTemplate.insert(user);
	}

	/**
	 * User to Get the User Information by Id
	 * 
	 * @param id
	 * @return {@link UserDao}
	 */
	@Override
	public User getUser(UUID id) {
		
		return cassandraTemplate.selectOneById(User.class, id);
	}

	/**
	 * User to Delete the User Information by id
	 * 
	 * @param id
	 */
	@Override
	public void deleteUser(UUID id) {
		cassandraTemplate.deleteById(User.class, id);
	}

	/**
	 * User to Delete the User Information by user
	 * 
	 * @param user
	 * @return {@link UserDao}
	 */
	@Override
	public User updateUser(User user) {
		return  cassandraTemplate.update(user);
	}

	/**
	 * User to get the all Users Information
	 * 
	 * @return {@link UserDao}
	 */
	@SuppressWarnings("deprecation")
	@Override
	public List<User> getAllUser() {

		return cassandraTemplate.selectAll(User.class);
	}

	/**
	 * User to Get the User Information by UserName
	 * 
	 * @param userName
	 * @return {@link UserDao}
	 */
	@Override
	public User findByUserName(String userName) {
		return userRepo.findByUserName(userName);
	}

	@Override
	public List<String> findAllUserNames() {
		return cassandraTemplate.select("select username from user", String.class); 
	}

	@Override
	public List<User> findUsersByRoleId(String id) {
		return userRepo.findUsersByRoleId(id);
	}

}
