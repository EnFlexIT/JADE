/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.security;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import starlight.util.Base64;

public class JADECertificate implements java.io.Serializable {
	
	JADEPrincipal subject;
	JADEPrincipal issuer;
	
	long serial;
	long notBefore;
	long notAfter;
	
	byte[] key;
	byte[] signature;
	
	public JADECertificate() {
  }

	public JADECertificate(JADECertificate cert) {
		this.subject = cert.subject;
		this.issuer = cert.issuer;
		this.notBefore = cert.notBefore;
		this.notAfter = cert.notAfter;
		this.key = cert.key;
		this.serial = cert.serial;
		this.signature = cert.signature;
	}
	
  //methods to (un)marshall

	public JADEPrincipal getSubject() { return subject; }
	public JADEPrincipal getIssuer() { return issuer; }
	public Long getSerialAsLong() { return new Long(serial); }
	public Long getNotBeforeAsLong() { return new Long(notBefore); }
	public Long getNotAfterAsLong() { return new Long(notAfter); }
	public String getPublicKeyAsString() { return new String(Base64.encode(key)); }
	public String getSignatureAsString() { return new String(Base64.encode(signature)); }
	
	public void setSubject(JADEPrincipal subject) { this.subject = subject; }
	public void setIssuer(JADEPrincipal issuer) { this.issuer = issuer; }
	public void setSerialAsLong(Long serial) { this.serial = serial.longValue(); }
	public void setNotBeforeAsLong(Long nb) { this.notBefore = nb.longValue(); }
	public void setNotAfterAsLong(Long na) { this.notAfter = na.longValue(); }
	public void setPublicKeyAsString(String key) { this.key = Base64.decode(key.toCharArray()); }
	public void setSignatureAsString(String signature) { this.signature = Base64.decode(signature.toCharArray()); }
	
	// other methods
	
	public byte[] getSignature() { return signature; }

	public void init(JADEPrincipal subject, long notBefore, long notAfter) {
		this.subject = subject;
		this.notBefore = notBefore;
		this.notAfter = notAfter;
	}
	
	public void issue(JADEPrincipal issuer, byte[] key, long serial) {
		this.issuer = issuer;
		this.serial = serial;
  	this.key = key;
	}
	
	public void sign(byte[] signature) {
		this.signature = signature;
	}
	
	public long getSerial() {
		return serial;
	}
	
	public BigInteger getSerialNumber() {
		return BigInteger.valueOf(serial);
	}
	
	public Date getNotBefore() {
		return new Date(notBefore);
	}
	
	public Date getNotAfter() {
		return new Date(notAfter);
	}
	
	public byte[] getEncoded() {
		StringBuffer str = new StringBuffer();
		str.append(subject.getName()).append('\n');
		str.append(issuer.getName()).append('\n');
		str.append(notBefore).append('\n');
		str.append(notAfter).append('\n');
		str.append(serial).append('\n');
		return str.toString().getBytes();
	}
	
	/*
	public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
		verify(key, null);
	}
	
	public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
			Signature sign = null;
			if (sigProvider != null)
				sign = Signature.getInstance("DSA", sigProvider);
			else
				sign = Signature.getInstance("DSA");
			sign.initVerify(key);
			sign.update(getEncoded());
			if (!sign.verify(getSignature())) throw new SignatureException();
	}
	*/
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(subject.getName()).append('\n');
		str.append(issuer.getName()).append('\n');
		str.append(notBefore).append('\n');
		str.append(notAfter).append('\n');
		str.append(serial).append('\n');
		str.append(getSignatureAsString()).append('\n');
		return str.toString();
	}
	
	public Object clone() {
		return new JADECertificate(this);
	}
	
	/*public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
		checkValidity(new Date());
	}
	
	public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
		if (notAfter != 0 && date.compareTo(getNotAfter())>=0)
			throw new CertificateExpiredException();
		if (notBefore != 0 && date.compareTo(getNotBefore())<=0)
			throw new CertificateNotYetValidException();
	}
	*/
}
