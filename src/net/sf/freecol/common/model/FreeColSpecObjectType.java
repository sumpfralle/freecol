/**
 *  Copyright (C) 2002-2016   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.model.Scope;
import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;
import static net.sf.freecol.common.util.CollectionUtils.*;


/**
 * The base class for all types defined by the specification. It can
 * be instantiated in order to provide a source for modifiers and
 * abilities that are provided by the code rather than defined in the
 * specification, such as the "artillery in the open" penalty.
 *
 * A FreeColSpecObjectType does not always need a reference to the
 * specification. However, if it has attributes or children that are
 * themselves FreeColSpecObjectTypes, then the specification must be
 * set before the type is de-serialized, otherwise the identifiers can
 * not be resolved.
 *
 * FreeColSpecObjectTypes can be abstract. Abstract types can be used
 * to derive other types, but can not be instantiated.  They will be
 * removed from the Specification after it has loaded completely.
 */
public abstract class FreeColSpecObjectType extends FreeColSpecObject
    implements Named {

    /** Whether the type is abstract, or can be instantiated. */
    private boolean abstractType;

    /**
     * The features of this game object type.  Feature containers are
     * created on demand.
     */
    private FeatureContainer featureContainer = null;

    /**
     * Scopes that might limit the action of this object to certain
     * types of objects.
     */
    private List<Scope> scopes = null;

    // Do not serialize below.

    /**
     * The index imposes a total ordering consistent with equals on
     * each class extending FreeColSpecObjectType, but this ordering
     * is nothing but the order in which the objects of the respective
     * class were defined.  It is guaranteed to remain stable only for
     * a particular revision of a particular specification.
     */
    private int index = -1;
    

    /**
     * Deliberately empty constructor.
     */
    protected FreeColSpecObjectType() {
        super(null);
    }

    /**
     * Create a simple FreeColSpecObjectType without a specification.
     *
     * @param id The object identifier.
     */
    public FreeColSpecObjectType(String id) {
        this(id, null);
    }

    /**
     * Create a FreeColSpecObjectType with a given specification but
     * no object identifier.
     *
     * @param specification The <code>Specification</code> to refer to.
     */
    public FreeColSpecObjectType(Specification specification) {
        this(null, specification);
    }

    /**
     * Create a FreeColSpecObjectType with a given identifier and
     * specification.
     *
     * @param id The object identifier.
     * @param specification The <code>Specification</code> to refer to.
     */
    public FreeColSpecObjectType(String id, Specification specification) {
        super(specification);

        setId(id);
    }


    /**
     * Get the scopes applicable to this effect.
     *
     * @return A list of <code>Scope</code>s.
     */
    public final List<Scope> getScopeList() {
        return (this.scopes == null) ? Collections.<Scope>emptyList()
            : this.scopes;
    }

    /**
     * Get the scopes applicable to this effect as a stream.
     *
     * @return A stream of <code>Scope</code>s.
     */
    public final Stream<Scope> getScopes() {
        return (this.scopes == null) ? Stream.<Scope>empty()
            : getScopeList().stream();
    }

    /**
     * Set the scopes for this object.
     *
     * @param scopes A list of new <code>Scope</code>s.
     */
    public final void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    /**
     * Add a scope.
     *
     * @param scope The <code>Scope</code> to add.
     */
    private void addScope(Scope scope) {
        if (this.scopes == null) this.scopes = new ArrayList<>();
        this.scopes.add(scope);
    }

    /**
     * Does at least one of this effect's scopes apply to an object type.
     *
     * @param objectType The <code>FreeColSpecObjectType</code> to check.
     * @return True if this effect applies.
     */
    public boolean appliesTo(final FreeColSpecObjectType objectType) {
        return appliesTo((FreeColObject)objectType);
    }

    /**
     * Does at least one of this effect's scopes apply to an object.
     *
     * @param object The <code>FreeColObject</code> to check.
     * @return True if this effect applies.
     */
    protected boolean appliesTo(FreeColObject object) {
        return (this.scopes == null || this.scopes.isEmpty()) ? true
            : any(this.scopes, s -> s.appliesTo(object));
    }

    /**
     * Gets the index of this FreeColSpecObjectType.
     *
     * The index imposes a total ordering consistent with equals on
     * each class extending FreeColSpecObjectType, but this ordering
     * is nothing but the order in which the objects of the respective
     * class were defined.  It is guaranteed to remain stable only for
     * a particular revision of a particular specification.
     *
     * @return The game object index.
     */
    protected int getIndex() {
        return this.index;
    }

    /**
     * Sets the index of this FreeColSpecObjectType.
     *
     * @param index The new index value.
     */
    protected final void setIndex(final int index) {
        this.index = index;
    }

    /**
     * Gets a string suitable for looking up the description of
     * this object in {@link net.sf.freecol.common.i18n.Messages}.
     *
     * @return A description key.
     */
    public final String getDescriptionKey() {
        return Messages.descriptionKey(getId());
    }

    /**
     * Is this an abstract type?
     *
     * @return True if this is an abstract game object type.
     */
    public final boolean isAbstractType() {
        return this.abstractType;
    }


    // Interface Named

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getNameKey() {
        return Messages.nameKey(getId());
    }


    // Override FreeColObject

    /**
     * {@inheritDoc}
     */
    @Override
    public final FeatureContainer getFeatureContainer() {
        if (this.featureContainer == null) {
            this.featureContainer = new FeatureContainer();
        }
        return this.featureContainer;
    }


    // Serialization

    // We do not serialize index, so no INDEX_TAG.
    // We do not need to write the abstractType attribute, as once
    // the spec is read, all cases of abstractType==true are removed.
    private static final String ABSTRACT_TAG = "abstract";
    // Denotes deletion of a child element.
    protected static final String DELETE_TAG = "delete";
    // Denotes that this type extends another.
    public static final String EXTENDS_TAG = "extends";
    // Denotes preservation of attributes and children.
    public static final String PRESERVE_TAG = "preserve";


    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeChildren(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeChildren(xw);

        for (Ability ability : sort(getAbilities())) ability.toXML(xw);

        for (Modifier modifier : getSortedModifiers()) modifier.toXML(xw);

        for (Scope scope : getScopeList()) scope.toXML(xw);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);
        if (getId() == null) throw new XMLStreamException("Null id");

        this.abstractType = xr.getAttribute(ABSTRACT_TAG, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChildren(FreeColXMLReader xr) throws XMLStreamException {
        // Clear containers.
        if (xr.shouldClearContainers()) {
            if (this.featureContainer != null) this.featureContainer.clear();
            setScopes(null);
        }

        super.readChildren(xr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChild(FreeColXMLReader xr) throws XMLStreamException {
        final Specification spec = getSpecification();
        final String tag = xr.getLocalName();

        if (Ability.getTagName().equals(tag)) {
            if (xr.getAttribute(DELETE_TAG, false)) {
                removeAbilities(xr.readId());
                xr.closeTag(Ability.getTagName());

            } else {
                Ability ability = new Ability(xr, spec); // Closes the element
                if (ability.getSource() == null) ability.setSource(this);
                addAbility(ability);
                spec.addAbility(ability);
            }

        } else if (Modifier.getTagName().equals(tag)) {
            if (xr.getAttribute(DELETE_TAG, false)) {
                removeModifiers(xr.readId());
                xr.closeTag(Modifier.getTagName());

            } else {
                Modifier modifier = new Modifier(xr, spec);// Closes the element
                if (modifier.getSource() == null) modifier.setSource(this);
                addModifier(modifier);
                spec.addModifier(modifier);
            }

        } else if (Scope.getTagName().equals(tag)) {
            Scope scope = new Scope(xr);
            if (scope != null) addScope(scope);

        } else {
            super.readChild(xr);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getId();
    }
}
