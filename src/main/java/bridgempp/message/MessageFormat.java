package bridgempp.message;

public class MessageFormat
{
	private MIMEProperty mimeProperty;
	private StorageProperty storageProperty;
	private EncodingProperty encodingProperty;
	
	public boolean equals(Object other)
	{
		if(other instanceof MessageFormat)
		{
			MessageFormat otherFormat = (MessageFormat) other;
			return otherFormat.getStorageProperty().equals(getStorageProperty()) &&
					otherFormat.getMimeProperty().equals(getMimeProperty()) &&
					otherFormat.getEncodingProperty().equals(encodingProperty);
		}
		return false;
	}
	
	
	/**
	 * @return the mimeProperty
	 */
	public MIMEProperty getMimeProperty()
	{
		return mimeProperty;
	}
	/**
	 * @param mimeProperty the mimeProperty to set
	 */
	public void setMimeProperty(MIMEProperty mimeProperty)
	{
		this.mimeProperty = mimeProperty;
	}
	/**
	 * @return the storageProperty
	 */
	public StorageProperty getStorageProperty()
	{
		return storageProperty;
	}
	/**
	 * @param storageProperty the storageProperty to set
	 */
	public void setStorageProperty(StorageProperty storageProperty)
	{
		this.storageProperty = storageProperty;
	}
	/**
	 * @return the encodingProperty
	 */
	public EncodingProperty getEncodingProperty()
	{
		return encodingProperty;
	}
	/**
	 * @param encodingProperty the encodingProperty to set
	 */
	public void setEncodingProperty(EncodingProperty encodingProperty)
	{
		this.encodingProperty = encodingProperty;
	}

}
