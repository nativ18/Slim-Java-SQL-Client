package datamanager.model;

import engine.Deserializer;
import engine.SqlBinder;

/**
 * An example class for creating a annotation based entity that
 * {@code GenericDao} can manage.
 *
 * @author Nativ Levy
 */
public class Customer extends BaseEntity {

	@SqlBinder(val = "first_name")
	public String firstName;
	@SqlBinder(val = "last_name")
	public String lastName;
	@SqlBinder(val = "location")
	public String location;
	@SqlBinder(val = "password")
	public String password;
	@SqlBinder(val = "birthday")
	public long birthdate;
	@SqlBinder(val = "gender")
	public int gender;
	@SqlBinder(val = "thumb_url")
	public String thumbUrl;

	@Deserializer
	public Customer(long id, String firstName, String lastName, String location, String password, long birthdate,
			Integer gender, String thumbUrl) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.location = location;
		this.password = password;
		this.birthdate = birthdate;
		this.gender = gender;
		this.thumbUrl = thumbUrl;
	}

	public Customer() {
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public String getFullname() {
		return String.format("%s %s", firstName, lastName);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(long birthdate) {
		this.birthdate = birthdate;
	}
}
