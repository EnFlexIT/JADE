/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.onto;

import jade.content.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import java.lang.reflect.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class ReflectiveIntrospector implements Introspector {

    /**
     * Externalize
     *
     * @param onto
     * @param obj
     *
     * @return
     *
     * @throws OntologyException
     *
     */
    public AbsObject externalise(Ontology onto, 
                                 Object obj) throws OntologyException {
        try {
            if (obj == null) {
                return null;
            } 

            if (obj instanceof String) {
                return AbsPrimitive.wrap((String) obj);
            } 

            if (obj instanceof Boolean) {
                return AbsPrimitive.wrap(((Boolean) obj).booleanValue());
            } 

            if (obj instanceof Float) {
                return AbsPrimitive.wrap(((Float) obj).floatValue());
            } 

            if (obj instanceof Integer) {
                return AbsPrimitive.wrap(((Integer) obj).intValue());
            } 

            if (obj instanceof List) {
                return AbsAggregate.fromObject((List) obj, onto);
            } 

            if (obj instanceof ContentElementList) {
                return AbsContentElementList.fromObject((List) obj, onto);
            } 
	    
	    if (obj instanceof Iterator) {
		return AbsAggregate.fromObject((Iterator) obj, onto);
	    }

            Class        javaClass = obj.getClass();
            ObjectSchema schema = onto.getElementSchema(javaClass);
            AbsObject    abs = schema.newInstance();
            Method[]     methods = javaClass.getMethods();
            String[]     names = schema.getAttributeNames();

            for (int i = 0; i < methods.length; i++) {
                String name = methods[i].getName();

                if (name.startsWith("get")) {
                    String attributeName = 
                        (name.substring(3, name.length())).toUpperCase();

                    if (schema.isAttribute(attributeName)) {
                        AbsObject attributeValue = invokeGetMethod(onto, 
                                                                   methods[i], 
                                                                   obj);

                        if (attributeValue != null) {
                            abs.setAttribute(attributeName, attributeValue);
                        } 
                    } 
                } 
            } 

            return abs;
        } 
        catch (OntologyException oe) {
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 

    private AbsObject invokeGetMethod(Ontology onto, Method method, 
                                      Object obj) throws OntologyException {
        try {
            Object result = method.invoke(obj, null);

            if (result == null) {
                return null;
            } 

            return externalise(onto, result);
        } 
        catch (Exception e) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 

    /**
     * Internalize
     *
     * @param onto
     * @param abs
     *
     * @return
     *
     * @throws OntologyException
     * @throws UngroundedException
     *
     */
    public Object internalise(Ontology onto, AbsObject abs) 
            throws UngroundedException, OntologyException {
        try {
            if (abs == null) {
                return null;
            } 

            if (abs instanceof AbsPrimitive) {
                return AbsPrimitive.toObject((AbsPrimitive) abs);
            } 

            if (abs instanceof AbsAggregate) {
                return ((AbsAggregate) abs).toObject(onto);
            } 

            if (abs instanceof AbsContentElementList) {
                return ((AbsContentElementList) abs).toObject(onto);
            } 

            Class        javaClass = onto.getElementClass(abs.getTypeName());
            Object       obj = javaClass.newInstance();
            ObjectSchema schema = onto.getElementSchema(javaClass);
            Method[]     methods = javaClass.getMethods();
            String[]     names = schema.getAttributeNames();

            for (int i = 0; i < methods.length; i++) {
                String name = methods[i].getName();

                if (name.startsWith("set")) {
                    String attributeName = 
                        (name.substring(3, name.length())).toUpperCase();

                    if (schema.isAttribute(attributeName)) {
                        AbsObject attributeValue = 
                            abs.getAttribute(attributeName);

                        if (attributeValue != null) {
                            invokeSetMethod(onto, methods[i], obj, 
                                            attributeValue);
                        } 
                    } 
                } 
            } 

            return obj;
        } 
        catch (OntologyException oe) {
            throw oe;
        } 
        catch (Throwable t) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 

    private void invokeSetMethod(Ontology onto, Method method, Object obj, 
                                 AbsObject value) throws OntologyException {
        try {
            Object objValue = internalise(onto, value);

            if (objValue == null) {
                return;
            } 

            Object[] params = new Object[] {
                objValue
            };

            method.invoke(obj, params);
        } 
        catch (Exception e) {
            throw new OntologyException("Schema and Java class do not match");
        } 
    } 

}

