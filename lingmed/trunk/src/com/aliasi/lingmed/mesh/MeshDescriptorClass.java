package com.aliasi.lingmed.mesh;

/**
 * A {@code MeshDescriptorClass} is an enumeration takeing on one of
 * four values depending on the type of the record.  
 *
 * <p>The four descriptor classes are (with descriptions repeated from
 * NLM's <a
 * href="http://www.nlm.nih.gov/mesh/xml_data_elements.html">MeSH XML
 * Element Descriptions</a>:
 *
 * <blockquote><table border="1" cellpadding="5">
 * <tr><th>MeshDescriptorClass</th><th>Description</th><th>Example</th><th>Tree Number</th></tr>
 * <tr><td><code>MeshDescriptorClass.ONE</code></td>
 *     <td>Topical Descriptor</td>
 *     <td>Calcimycin</td>
 *     <td>Yes (D prefix)</td></tr>
 * <tr><td><code>MeshDescriptorClass.TWO</code></td>
 *     <td>Publication Types</td>
 *     <td>Review [Publication Type]</td>
 *     <td>No</td></tr>
 * <tr><td><code>MeshDescriptorClass.THREE</code></td>
 *     <td>Check Tags</td>
 *     <td>Male</td>
 *     <td>No</td></tr>
 * <tr><td><code>MeshDescriptorClass.FOUR</code></td>
 *     <td>Geographic Descriptor</td>
 *     <td>Washington</td>
 *     <td>Yes (Z prefix)</td></tr>
 * </table></blockquote>
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public enum MeshDescriptorClass {

    /** 
     * The value for topical descriptors.
     */
    ONE(1,"Topical Descriptor"), 
        
    /**
     * The value for publication types.
     */
    TWO(2,"Publication Types"), 

    /**
     * The value for check tags.
     */
    THREE(3,"Check Tag"), 
        
    /**
     * The value for geographic descriptors.
     */
    FOUR(4,"Geographic Descriptor");
        
    private final int mNumericalValue;
    private final String mDescription;

    MeshDescriptorClass(int numericalValue, String description) {
        mNumericalValue = numericalValue;
        mDescription = description;
    }

    /**
     * Returns the description of this descriptor class.
     *
     * @return Description of this descriptor class.
     */
    public String description() {
        return mDescription;
    }

    /**
     * Returns a string-based representation of the descriptor
     * class, including numerical value and description.
     *
     * @return String representation of this class.
     */
    @Override
    public String toString() {
        return Integer.toString(mNumericalValue)
            + "(" + description() + ")";
    }

}