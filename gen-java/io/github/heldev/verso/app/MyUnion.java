/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package io.github.heldev.verso.app;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2021-01-03")
public class MyUnion extends org.apache.thrift.TUnion<MyUnion, MyUnion._Fields> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MyUnion");
  private static final org.apache.thrift.protocol.TField MY_UNION_FIELD_DESC = new org.apache.thrift.protocol.TField("myUnion", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField MY_END_FIELD_DESC = new org.apache.thrift.protocol.TField("myEnd", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField HLIST_FIELD_DESC = new org.apache.thrift.protocol.TField("hlist", org.apache.thrift.protocol.TType.STRUCT, (short)3);

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    MY_UNION((short)1, "myUnion"),
    MY_END((short)2, "myEnd"),
    HLIST((short)3, "hlist");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // MY_UNION
          return MY_UNION;
        case 2: // MY_END
          return MY_END;
        case 3: // HLIST
          return HLIST;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MY_UNION, new org.apache.thrift.meta_data.FieldMetaData("myUnion", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT        , "MyUnion")));
    tmpMap.put(_Fields.MY_END, new org.apache.thrift.meta_data.FieldMetaData("myEnd", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.HLIST, new org.apache.thrift.meta_data.FieldMetaData("hlist", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT        , "HList")));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MyUnion.class, metaDataMap);
  }

  public MyUnion() {
    super();
  }

  public MyUnion(_Fields setField, java.lang.Object value) {
    super(setField, value);
  }

  public MyUnion(MyUnion other) {
    super(other);
  }
  public MyUnion deepCopy() {
    return new MyUnion(this);
  }

  public static MyUnion myUnion(MyUnion value) {
    MyUnion x = new MyUnion();
    x.setMyUnion(value);
    return x;
  }

  public static MyUnion myEnd(java.lang.String value) {
    MyUnion x = new MyUnion();
    x.setMyEnd(value);
    return x;
  }

  public static MyUnion hlist(HList value) {
    MyUnion x = new MyUnion();
    x.setHlist(value);
    return x;
  }


  @Override
  protected void checkType(_Fields setField, java.lang.Object value) throws java.lang.ClassCastException {
    switch (setField) {
      case MY_UNION:
        if (value instanceof MyUnion) {
          break;
        }
        throw new java.lang.ClassCastException("Was expecting value of type MyUnion for field 'myUnion', but got " + value.getClass().getSimpleName());
      case MY_END:
        if (value instanceof java.lang.String) {
          break;
        }
        throw new java.lang.ClassCastException("Was expecting value of type java.lang.String for field 'myEnd', but got " + value.getClass().getSimpleName());
      case HLIST:
        if (value instanceof HList) {
          break;
        }
        throw new java.lang.ClassCastException("Was expecting value of type HList for field 'hlist', but got " + value.getClass().getSimpleName());
      default:
        throw new java.lang.IllegalArgumentException("Unknown field id " + setField);
    }
  }

  @Override
  protected java.lang.Object standardSchemeReadValue(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TField field) throws org.apache.thrift.TException {
    _Fields setField = _Fields.findByThriftId(field.id);
    if (setField != null) {
      switch (setField) {
        case MY_UNION:
          if (field.type == MY_UNION_FIELD_DESC.type) {
            MyUnion myUnion;
            myUnion = new MyUnion();
            myUnion.read(iprot);
            return myUnion;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case MY_END:
          if (field.type == MY_END_FIELD_DESC.type) {
            java.lang.String myEnd;
            myEnd = iprot.readString();
            return myEnd;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        case HLIST:
          if (field.type == HLIST_FIELD_DESC.type) {
            HList hlist;
            hlist = new HList();
            hlist.read(iprot);
            return hlist;
          } else {
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            return null;
          }
        default:
          throw new java.lang.IllegalStateException("setField wasn't null, but didn't match any of the case statements!");
      }
    } else {
      org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
      return null;
    }
  }

  @Override
  protected void standardSchemeWriteValue(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    switch (setField_) {
      case MY_UNION:
        MyUnion myUnion = (MyUnion)value_;
        myUnion.write(oprot);
        return;
      case MY_END:
        java.lang.String myEnd = (java.lang.String)value_;
        oprot.writeString(myEnd);
        return;
      case HLIST:
        HList hlist = (HList)value_;
        hlist.write(oprot);
        return;
      default:
        throw new java.lang.IllegalStateException("Cannot write union with unknown field " + setField_);
    }
  }

  @Override
  protected java.lang.Object tupleSchemeReadValue(org.apache.thrift.protocol.TProtocol iprot, short fieldID) throws org.apache.thrift.TException {
    _Fields setField = _Fields.findByThriftId(fieldID);
    if (setField != null) {
      switch (setField) {
        case MY_UNION:
          MyUnion myUnion;
          myUnion = new MyUnion();
          myUnion.read(iprot);
          return myUnion;
        case MY_END:
          java.lang.String myEnd;
          myEnd = iprot.readString();
          return myEnd;
        case HLIST:
          HList hlist;
          hlist = new HList();
          hlist.read(iprot);
          return hlist;
        default:
          throw new java.lang.IllegalStateException("setField wasn't null, but didn't match any of the case statements!");
      }
    } else {
      throw new org.apache.thrift.protocol.TProtocolException("Couldn't find a field with field id " + fieldID);
    }
  }

  @Override
  protected void tupleSchemeWriteValue(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    switch (setField_) {
      case MY_UNION:
        MyUnion myUnion = (MyUnion)value_;
        myUnion.write(oprot);
        return;
      case MY_END:
        java.lang.String myEnd = (java.lang.String)value_;
        oprot.writeString(myEnd);
        return;
      case HLIST:
        HList hlist = (HList)value_;
        hlist.write(oprot);
        return;
      default:
        throw new java.lang.IllegalStateException("Cannot write union with unknown field " + setField_);
    }
  }

  @Override
  protected org.apache.thrift.protocol.TField getFieldDesc(_Fields setField) {
    switch (setField) {
      case MY_UNION:
        return MY_UNION_FIELD_DESC;
      case MY_END:
        return MY_END_FIELD_DESC;
      case HLIST:
        return HLIST_FIELD_DESC;
      default:
        throw new java.lang.IllegalArgumentException("Unknown field id " + setField);
    }
  }

  @Override
  protected org.apache.thrift.protocol.TStruct getStructDesc() {
    return STRUCT_DESC;
  }

  @Override
  protected _Fields enumForId(short id) {
    return _Fields.findByThriftIdOrThrow(id);
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }


  public MyUnion getMyUnion() {
    if (getSetField() == _Fields.MY_UNION) {
      return (MyUnion)getFieldValue();
    } else {
      throw new java.lang.RuntimeException("Cannot get field 'myUnion' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setMyUnion(MyUnion value) {
    if (value == null) throw new java.lang.NullPointerException();
    setField_ = _Fields.MY_UNION;
    value_ = value;
  }

  public java.lang.String getMyEnd() {
    if (getSetField() == _Fields.MY_END) {
      return (java.lang.String)getFieldValue();
    } else {
      throw new java.lang.RuntimeException("Cannot get field 'myEnd' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setMyEnd(java.lang.String value) {
    if (value == null) throw new java.lang.NullPointerException();
    setField_ = _Fields.MY_END;
    value_ = value;
  }

  public HList getHlist() {
    if (getSetField() == _Fields.HLIST) {
      return (HList)getFieldValue();
    } else {
      throw new java.lang.RuntimeException("Cannot get field 'hlist' because union is currently set to " + getFieldDesc(getSetField()).name);
    }
  }

  public void setHlist(HList value) {
    if (value == null) throw new java.lang.NullPointerException();
    setField_ = _Fields.HLIST;
    value_ = value;
  }

  public boolean isSetMyUnion() {
    return setField_ == _Fields.MY_UNION;
  }


  public boolean isSetMyEnd() {
    return setField_ == _Fields.MY_END;
  }


  public boolean isSetHlist() {
    return setField_ == _Fields.HLIST;
  }


  public boolean equals(java.lang.Object other) {
    if (other instanceof MyUnion) {
      return equals((MyUnion)other);
    } else {
      return false;
    }
  }

  public boolean equals(MyUnion other) {
    return other != null && getSetField() == other.getSetField() && getFieldValue().equals(other.getFieldValue());
  }

  @Override
  public int compareTo(MyUnion other) {
    int lastComparison = org.apache.thrift.TBaseHelper.compareTo(getSetField(), other.getSetField());
    if (lastComparison == 0) {
      return org.apache.thrift.TBaseHelper.compareTo(getFieldValue(), other.getFieldValue());
    }
    return lastComparison;
  }


  @Override
  public int hashCode() {
    java.util.List<java.lang.Object> list = new java.util.ArrayList<java.lang.Object>();
    list.add(this.getClass().getName());
    org.apache.thrift.TFieldIdEnum setField = getSetField();
    if (setField != null) {
      list.add(setField.getThriftFieldId());
      java.lang.Object value = getFieldValue();
      if (value instanceof org.apache.thrift.TEnum) {
        list.add(((org.apache.thrift.TEnum)getFieldValue()).getValue());
      } else {
        list.add(value);
      }
    }
    return list.hashCode();
  }
  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }


  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }


}
