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
import java.math.BigDecimal;
import java.util.Date;

/**
 * MEMBERSテーブルに対応したEntity
 */
@Entity
@Table(name = "DAO_MEMBERS")
public class Members {

    @Id
    @Column(name = "MEMBER_ID", length = 15)
    public Long id;

    @Column(name = "STRING_COL", length = 100)
    public String stringCol;

    @Column(name = "DATE_COL")
    @Temporal(TemporalType.DATE)
    public Date dateCol;

    @Column(name = "TIMESTAMP_COL")
    @Temporal(TemporalType.TIMESTAMP)
    public Date timestampCol;

    @Column(name = "BIG_DECIMAL_COL", precision = 15, scale = 10)
    public BigDecimal bigDecimalCol;

    @Column(name = "INTEGER_COL", length = 15)
    public Integer integerCol;

    @Column(name = "FLOAT_COL", length = 15)
    public Float floatCol;

    @Column(name = "DOUBLE_COL", length = 15)
    public Double doubleCol;

    @Column(name = "SHORT_COL", length = 15)
    public Short shortCol;

    @Column(name = "bool1")
    public Boolean bool1 = Boolean.FALSE;

    @Column(name = "bool2")
    public Boolean bool2 = Boolean.FALSE;

    @Column(name = "bool3")
    public Boolean bool3 = Boolean.FALSE;

    @Column(name = "bool4")
    public Boolean bool4 = Boolean.FALSE;

    public Members() {
    }

    public Members(Long id) {
        this.id = id;
    }

    public Members(Long id, String stringCol) {
        this.id = id;
        this.stringCol = stringCol;
    }

    public Members(Long id, String stringCol, BigDecimal bigDecimalCol, Integer integerCol, Float floatCol, Double doubleCol, Short shortCol) {
        this.id = id;
        this.stringCol = stringCol;
        this.bigDecimalCol = bigDecimalCol;
        this.integerCol = integerCol;
        this.floatCol = floatCol;
        this.doubleCol = doubleCol;
        this.shortCol = shortCol;
    }

    public Members(Long id, String stringCol, Date dateCol, Date timestampCol, BigDecimal bigDecimalCol, int integerCol, float floatCol, double doubleCol, short shortCol, Boolean bool1, Boolean bool2, Boolean bool3, Boolean bool4) {
        this.id = id;
        this.stringCol = stringCol;
        this.dateCol = dateCol;
        this.timestampCol = timestampCol;
        this.bigDecimalCol = bigDecimalCol;
        this.integerCol = integerCol;
        this.floatCol = floatCol;
        this.doubleCol = doubleCol;
        this.shortCol = shortCol;
        this.bool1 = bool1;
        this.bool2 = bool2;
        this.bool3 = bool3;
        this.bool4 = bool4;
    }

    @Id
    @Column(name = "MEMBER_ID", length = 15)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(name = "seq", sequenceName = "MEMBER_ID_SEQ")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStringCol() {
        return stringCol;
    }

    public void setStringCol(String stringCol) {
        this.stringCol = stringCol;
    }

    @Temporal(TemporalType.DATE)
    public Date getDateCol() {
        return dateCol;
    }

    public void setDateCol(Date dateCol) {
        this.dateCol = dateCol;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getTimestampCol() {
        return timestampCol;
    }

    public void setTimestampCol(Date timestampCol) {
        this.timestampCol = timestampCol;
    }

    public BigDecimal getBigDecimalCol() {
        return bigDecimalCol;
    }

    public void setBigDecimalCol(BigDecimal bigDecimalCol) {
        this.bigDecimalCol = bigDecimalCol;
    }

    public int getIntegerCol() {
        return integerCol;
    }

    public void setIntegerCol(int integerCol) {
        this.integerCol = integerCol;
    }

    public float getFloatCol() {
        return floatCol;
    }

    public void setFloatCol(float floatCol) {
        this.floatCol = floatCol;
    }

    public double getDoubleCol() {
        return doubleCol;
    }

    public void setDoubleCol(double doubleCol) {
        this.doubleCol = doubleCol;
    }

    public short getShortCol() {
        return shortCol;
    }

    public void setShortCol(short shortCol) {
        this.shortCol = shortCol;
    }

    public Boolean getBool1() {
        return bool1;
    }

    public void setBool1(Boolean bool1) {
        this.bool1 = bool1;
    }

    public Boolean getBool2() {
        return bool2;
    }

    public void setBool2(Boolean bool2) {
        this.bool2 = bool2;
    }

    public Boolean getBool3() {
        return bool3;
    }

    public void setBool3(Boolean bool3) {
        this.bool3 = bool3;
    }

    public Boolean getBool4() {
        return bool4;
    }

    public void setBool4(Boolean bool4) {
        this.bool4 = bool4;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//
//        Members users = (Members) o;
//
//        if (id != null ? !id.equals(users.id) : users.id != null) {
//            return false;
//        }
//        if (name != null ? !name.equals(users.name) : users.name != null) {
//            return false;
//        }
//        if (birthday != null ? !birthday.equals(users.birthday) : users.birthday != null) {
//            return false;
//        }
//        if (insertDate != null ? !insertDate.equals(users.insertDate) : users.insertDate != null) {
//            return false;
//        }
//        if (version != null ? !version.equals(users.version) : users.version != null) {
//            return false;
//        }
//        return !(active != null ? !active.equals(users.active) : users.active != null);
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = id != null ? id.hashCode() : 0;
//        result = 31 * result + (name != null ? name.hashCode() : 0);
//        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
//        result = 31 * result + (insertDate != null ? insertDate.hashCode() : 0);
//        result = 31 * result + (version != null ? version.hashCode() : 0);
//        result = 31 * result + (active != null ? active.hashCode() : 0);
//        return result;
//    }
}


