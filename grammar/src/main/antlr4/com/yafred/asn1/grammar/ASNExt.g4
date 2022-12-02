grammar ASNExt;

import ASN;

/* ------------------ Overrides ---------------------------*/
assignment:
    typeAssignment
    | valueAssignment
//  | xMLValueAssignment
    | valueSetTypeAssignment
    | objectClassAssignment
    | objectAssignment
    | objectSetAssignment
    | parameterizedAssignment
;

builtinType:
    bitStringType
    | BOOLEAN_LITERAL // BooleanType
    | characterStringType
    | choiceType
    | DATE_LITERAL  // DateType
    | DATE_TIME_LITERAL // DateTimeType
    | DURATION_LITERAL  // DurationType
    | EMBEDDED_LITERAL PDV_LITERAL // EmbeddedPDVType
    | enumeratedType
    | EXTERNAL_LITERAL // ExternalType
    | instanceOfType // (Rec. ITU-T X.681 | ISO/IEC 8824-2, Annex C)
    | integerType
    | OID_IRI_LITERAL // IRIType
    | NULL_LITERAL // NullType
    | objectClassFieldType // (Rec. ITU-T X.681 | ISO/IEC 8824-2, 14.1)
    | OBJECT_LITERAL IDENTIFIER_LITERAL // ObjectIdentifierType
    | OCTET_LITERAL STRING_LITERAL // OctetStringType
    | REAL_LITERAL  // RealType
    | RELATIVE_OID_IRI_LITERAL // RelativeIRIType
    | RELATIVE_OID_LITERAL // RelativeOIDType
    | sequenceType
    | sequenceOfType
    | setType
    | setOfType
    | prefixedType
    | TIME_LITERAL // TimeType
    | TIME_OF_DAY_LITERAL // TimeOfDayType
;


/*---------------------- Value Set --------------------------------------------------*/

valueSetTypeAssignment:
    UCASE_ID type ASSIGN valueSet
;

valueSet:
    LCURLY elementSetSpec RCURLY
;



/*--------------------- Object Classes ----------------------------------------------*/

definedObjectClass:
    externalObjectClassReference
    | OBJECT_CLASS_REFERENCE
    | usefulObjectClassReference
;

definedObject:
    externalObjectReference
    | UCASE_ID
;

definedObjectSet:
    externalObjectSetReference
    | UCASE_ID
;

externalObjectClassReference:
    UCASE_ID /* modulereference */ DOT OBJECT_CLASS_REFERENCE
;

externalObjectReference:
    UCASE_ID /* modulereference */ DOT UCASE_ID
;

externalObjectSetReference:
    UCASE_ID /* modulereference */ DOT UCASE_ID
;

usefulObjectClassReference:
    typeIdentifier
//    | abstractSyntax
;

typeIdentifier:
    CLASS_LITERAL
    LCURLY
    '&id' OBJECT_LITERAL IDENTIFIER_LITERAL UNIQUE_LITERAL COMMA
    '&Type'
    RCURLY
    WITH_LITERAL SYNTAX_LITERAL LCURLY '&Type' IDENTIFIED_BY_LITERAL '&id' RCURLY
;



objectClassAssignment:
    OBJECT_CLASS_REFERENCE ASSIGN objectClass
;

objectClass:
    definedObjectClass
    | objectClassDefn
//    | parameterizedObjectClass
;

objectClassDefn:
    CLASS_LITERAL LCURLY fieldSpec (COMMA fieldSpec)* RCURLY withSyntaxSpec?
;

withSyntaxSpec:
    WITH_LITERAL SYNTAX_LITERAL syntaxList
;

fieldSpec:
    typeFieldSpec
    | fixedTypeValueFieldSpec
    | variableTypeValueFieldSpec
    | fixedTypeValueSetFieldSpec
    | variableTypeValueSetFieldSpec
    | objectFieldSpec
    | objectSetFieldSpec
;

typeFieldSpec:
    UCASE_REF typeOptionalitySpec?
;

typeOptionalitySpec:
    OPTIONAL_LITERAL
    | DEFAULT_LITERAL type
;

fixedTypeValueFieldSpec:
    LCASE_REF type UNIQUE_LITERAL? valueOptionalitySpec?
;

valueOptionalitySpec:
    OPTIONAL_LITERAL
    | DEFAULT_LITERAL value
;

variableTypeValueFieldSpec:
    LCASE_REF fieldName valueOptionalitySpec?
;

fixedTypeValueSetFieldSpec:
    UCASE_REF type valueSetOptionalitySpec?
;

valueSetOptionalitySpec:
    OPTIONAL_LITERAL | DEFAULT_LITERAL valueSet
;

variableTypeValueSetFieldSpec:
    UCASE_REF fieldName valueSetOptionalitySpec?
;

objectFieldSpec:
    LCASE_REF definedObjectClass objectOptionalitySpec?
;

objectOptionalitySpec:
    OPTIONAL_LITERAL | DEFAULT_LITERAL object
;

objectSetFieldSpec:
    UCASE_REF definedObjectClass objectSetOptionalitySpec?
;

objectSetOptionalitySpec:
    OPTIONAL_LITERAL | DEFAULT_LITERAL objectSet
;

primitiveFieldName:
    UCASE_REF
    | LCASE_REF
;

fieldName:
    primitiveFieldName (DOT primitiveFieldName)*
;

syntaxList:
    LCURLY tokenOrGroupSpec (tokenOrGroupSpec)* RCURLY
;

tokenOrGroupSpec:
    requiredToken | optionalGroup
;

optionalGroup:
    LBRACKET tokenOrGroupSpec (tokenOrGroupSpec)* RBRACKET
;

requiredToken:
    literal | primitiveFieldName
;

literal:
    UCASE_ID | COMMA
;

objectAssignment:
    LCASE_ID definedObjectClass ASSIGN object
;

object:
    definedObject
    | objectDefn
    | informationFromObjects
//    | parameterizedObject
;

objectDefn:
    defaultSyntax
    | definedSyntax
;



defaultSyntax:
    LCURLY fieldSetting (COMMA fieldSetting)* RCURLY
;

fieldSetting:
    primitiveFieldName setting
;

definedSyntax:
    LCURLY definedSyntaxToken (definedSyntaxToken)* RCURLY
;

definedSyntaxToken:
    literal
    | setting
;

setting:
    type
    | value
    | valueSet
    | object
    | objectSet
;

objectSetAssignment:
    UCASE_ID definedObjectClass ASSIGN objectSet
;

objectSet:
    LCURLY objectSetAssignment RCURLY
;

//objectSetSpec:
//    rootElementSetSpec
//    | rootElementSetSpec COMMA ELLIPSIS
//    | ELLIPSIS
//    | ELLIPSIS COMMA additionalElementSetSpec
//    | rootElementSetSpec COMMA ELLIPSIS COMMA additionalElementSetSpec
//;

//objectSetElements:
//    object
//    | definedObjectSet
//    | informationFromObjects
////    | parameterizedObjectSet
//;

objectClassFieldType:
    definedObjectClass DOT fieldName
;

//objectClassFieldValue:
//    openTypeFieldVal
//    | fixedTypeFieldVal
//;

//openTypeFieldVal:
//    type COLON value
//;

//fixedTypeFieldVal:
//    builtinValue
//    | referencedValue
//;

informationFromObjects:
    referencedObjects DOT fieldName
;

//valueFromObject:
//    referencedObjects DOT fieldName
//;
//
//valueSetFromObjects:
//    referencedObjects DOT fieldName
//;
//
//typeFromObject:
//    referencedObjects DOT fieldName
//;
//
//objectFromObject:
//    referencedObjects DOT fieldName
//;
//
//objectSetFromObjects:
//    referencedObjects DOT fieldName
//;

referencedObjects:
    definedObject
//    | parameterizedObject
    | definedObjectSet
//    | parameterizedObjectSet
;

instanceOfType:
    INSTANCE_LITERAL OF_LITERAL definedObjectClass
;

//instanceOfValue:
//    value
//;

/*----------------------- Parameterization -------------------------------------------*/
// See ITU-T X.693 (02/2021) Paramaterization of ASN.1 specifications

parameterizedAssignment:
    parameterizedTypeAssignment
    | parameterizedValueAssignment
    | parameterizedObjectClassAssignment
    | parameterizedObjectAssignment
    | parameterizedObjectSetAssignment
;

parameterizedTypeAssignment:
    UCASE_ID parameterList ASSIGN type
;

parameterizedValueAssignment:
    LCASE_ID parameterList type ASSIGN value
;

parameterizedValueSetTypeAssignment:
    UCASE_ID parameterList type ASSIGN valueSet
;

parameterizedObjectClassAssignment:
    OBJECT_CLASS_REFERENCE parameterList ASSIGN objectClass
;

parameterizedObjectAssignment:
    LCASE_ID parameterList definedObjectClass ASSIGN object
;

parameterizedObjectSetAssignment:
    UCASE_ID parameterList definedObjectClass ASSIGN objectSet
;

parameterList:
    LCURLY parameter (COMMA parameter)* RCURLY
;

parameter:
    paramGovernor COLON dummyReference
    | dummyReference
;

paramGovernor:
    governor
    | dummyGovernor
;

governor:
    type
    | definedObjectClass
;

dummyGovernor:
    dummyReference
;

dummyReference:
    REFERENCE
;



/*--------------------- LITERAL -----------------------------------------------------*/

CLASS_LITERAL:
    'CLASS'
;

UNIQUE_LITERAL:
    'UNIQUE'
;

IDENTIFIED_LITERAL:
    'IDENTIFIED'
;

BY_LITERAL:
    'BY'
;

IDENTIFIED_BY_LITERAL:
    IDENTIFIED_LITERAL BY_LITERAL
;

SYNTAX_LITERAL:
    'SYNTAX'
;

INSTANCE_LITERAL:
    'INSTANCE'
;

/*--------------------- Lexical Items -----------------------------------------------*/

AMPERSAND:
    '&'
;

/* objectclassreference ITU-T X.681 (02/2021), like typereference with no lower case letters */
OBJECT_CLASS_REFERENCE:
    ('A'..'Z') ('-'('A'..'Z'|'0'..'9')|('A'..'Z'|'0'..'9'))*
;

/* Simplified definition of 'Reference' from ITU-T X.680 section 13.1 */
REFERENCE:
    UCASE_ID
    | LCASE_ID
    | OBJECT_CLASS_REFERENCE
;

/* typefieldreference ITU-T X.681 (02/2021) */
UCASE_REF:
    AMPERSAND UCASE_ID
;

/* valuefieldreference ITU-T X.681 (02/2021) */
LCASE_REF:
    AMPERSAND LCASE_ID
;