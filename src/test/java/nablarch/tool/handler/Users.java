package nablarch.tool.handler;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import java.util.Date;

/** USERSテーブルに対応したEntity */
@Entity
@Table(name = "DAO_USERS")
public class Users {

    @Id
    @Column(name = "USER_ID", length = 15)
    public Long id;

    @Column(name = "NAME", length = 100)
    public String name;

    @Column(name = "BIRTHDAY")
    @Temporal(TemporalType.DATE)
    public Date birthday;

    @Column(name = "INSERT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date insertDate;

    @Column(name = "VERSION", length = 18)
    public Long version;

    @Column(name = "active")
    public Boolean active = Boolean.FALSE;

    public Users() {
    }

    public Users(Long id) {
        this.id = id;
    }

    public Users(Long id, String name, Date birthday, Date insertDate) {
        this.birthday = birthday;
        this.insertDate = insertDate;
        this.id = id;
        this.name = name;
    }

    public Users(Long id, String name, Date birthday, Date insertDate, Long version) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.insertDate = insertDate;
        this.version = version;
    }

    public Users(Long id, String name, Date birthday, Date insertDate, Long version, boolean active) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.insertDate = insertDate;
        this.version = version;
        this.active = active;
    }

    @Id
    @Column(name = "USER_ID", length = 15)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(name = "seq", sequenceName = "USER_ID_SEQ")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Temporal(TemporalType.DATE)
    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }

    @Version
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Users users = (Users) o;

        if (id != null ? !id.equals(users.id) : users.id != null) {
            return false;
        }
        if (name != null ? !name.equals(users.name) : users.name != null) {
            return false;
        }
        if (birthday != null ? !birthday.equals(users.birthday) : users.birthday != null) {
            return false;
        }
        if (insertDate != null ? !insertDate.equals(users.insertDate) : users.insertDate != null) {
            return false;
        }
        if (version != null ? !version.equals(users.version) : users.version != null) {
            return false;
        }
        return !(active != null ? !active.equals(users.active) : users.active != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        result = 31 * result + (insertDate != null ? insertDate.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        return result;
    }
}


