/**
 * @typedef {Object} Term
 * @property {string} title
 * @property {string} simpleText
 * @property {string} kind
 * @property {string} description
 */

/**
 * @typedef {Object} TypeRef
 * @property {string} fqn
 * @property {TypeRef[]} [typeArgumentRefs]
 */

/**
 * @typedef {Object} DomainField
 * @property {string} name
 * @property {TypeRef} typeRef
 * @property {boolean} isDeprecated
 */

/**
 * @typedef {Object} MethodParameter
 * @property {string} name
 * @property {string} nameSource
 * @property {TypeRef} typeRef
 */

/**
 * @typedef {Object} DomainMethod
 * @property {string} fqn
 * @property {MethodParameter[]} parameters
 * @property {TypeRef} returnTypeRef
 * @property {boolean} isDeprecated
 */

/**
 * @typedef {Object} EnumConstant
 * @property {string} name
 * @property {string[]} params
 */

/**
 * @typedef {Object} EnumInfo
 * @property {EnumConstant[]} constants
 * @property {string[]} parameterNames
 */

/**
 * @typedef {Object} DomainType
 * @property {string} fqn
 * @property {DomainField[]} fields
 * @property {DomainMethod[]} methods
 * @property {DomainMethod[]} staticMethods
 * @property {EnumInfo} [enumInfo]
 * @property {boolean} isDeprecated
 */

/**
 * @typedef {Object} PackageType
 * @property {string} fqn
 * @property {{fqn: string}[]} types
 */

/**
 * @typedef {Object} DomainData
 * @property {string[]} domainPackageRoots
 * @property {DomainType[]} types
 * @property {PackageType[]} _packages
 * @property {Map<string, DomainType>} _typesMap
 * @property {Map<string, PackageType[]>} _childPackagesMap
 */

/**
 * @typedef {Object} Relation
 * @property {string} from - 依存元のFQN
 * @property {string} to - 依存先のFQN
 */

/**
 * @typedef {Object} Package
 * @property {string} fqn - パッケージの完全修飾名
 */

/**
 * @typedef {Object} CreatePackageLevelDiagramOptions
 * @property {boolean} [transitiveReductionEnabled] - 推移的縮約を行うかどうか
 * @property {string} diagramDirection - 図の向き ('TB' または 'LR')
 */

/**
 * @typedef {Object} MermaidDiagramSourceOptions
 * @property {string} diagramDirection - 図の向き ('TB' または 'LR')
 * @property {string|null} [focusedPackageFqn] - フォーカスされたパッケージ
 * @property {string|null} [clickHandlerName] - クリックハンドラ関数名
 */

/**
 * @typedef {Object} DiagramNodeLinesOptions
 * @property {Map<string, string>} nodeIdToFqn
 * @property {Map<string, string>} nodeLabelById
 * @property {Function} escapeMermaidText
 * @property {string|null} [clickHandlerName]
 * @property {Set<string>} parentFqnsWithRelations
 */

/**
 * @typedef {Object} OutboundPortOperation
 * @property {string} fqn
 * @property {string} signature
 */

/**
 * @typedef {Object} OutboundAdapter
 * @property {string} fqn
 */

/**
 * @typedef {Object} OutboundAdapterExecution
 * @property {string} fqn
 */

/**
 * @typedef {Object} PackageData
 * @property {Package[]} packages
 * @property {Relation[]} relations
 * @property {string} domainPackageRoots
 */

/**
 * @typedef {Object} JigField
 * @property {string} name
 * @property {TypeRef} typeRef
 * @property {boolean} isDeprecated
 */

/**
 * @typedef {Object} UsecaseMethod
 * @property {string} fqn
 * @property {string} visibility
 * @property {MethodParameter[]} parameters
 * @property {TypeRef} returnTypeRef
 * @property {boolean} isDeprecated
 * @property {string[]} callMethods 呼び出しているメソッドのFQN
 * @property {string} [kind] 内部で使用する種別 ("usecase" | "method" | "static-method" | "inbound-class" | "outbound" | "domain-type")
 */

/**
 * @typedef {Object} Usecase
 * @property {string} fqn
 * @property {JigField[]} fields
 * @property {UsecaseMethod[]} staticMethods
 * @property {UsecaseMethod[]} methods
 */

/**
 * @typedef {Object} UsecaseData
 * @property {Usecase[]} usecases
 */

/**
 * @typedef {Object} OutboundOperation
 * @property {string} fqn
 */

/**
 * @typedef {Object} OutboundPort
 * @property {OutboundOperation[]} [operations]
 */

/**
 * @typedef {Object} OutboundData
 * @property {OutboundPort[]} [outboundPorts]
 */

/**
 * @typedef {Object} Controller
 * @property {Relation[]} [relations]
 */

/**
 * @typedef {Object} InboundData
 * @property {Controller[]} [inboundAdapters]
 */

/**
 * @typedef {Object} DiagramContext
 * @property {Map<string, UsecaseMethod>} methodMap
 * @property {Set<string>} outboundOperationSet
 * @property {boolean} showDiagramInternalMethods
 * @property {boolean} showDiagramOutboundPorts
 * @property {boolean} showDiagramDomainTypes
 */

/**
 * @typedef {Object} DiagramNode
 * @property {string} fqn
 * @property {string} kind
 */

/**
 * @typedef {Object} DiagramEdge
 * @property {string} from
 * @property {string} to
 * @property {boolean} [dotted]
 */

/**
 * @typedef {Object} SequenceParticipant
 * @property {string} id
 * @property {string} label
 * @property {string} kind
 */

/**
 * @typedef {Object} SequenceCall
 * @property {string} from
 * @property {string} to
 * @property {string} label
 */

/**
 * @typedef {Object} SequenceDiagram
 * @property {SequenceParticipant[]} participants
 * @property {SequenceCall[]} calls
 */

/**
 * @typedef {Object} ScrollInfo
 * @property {string} [id]
 * @property {number} [offset]
 * @property {number} [scrollTop]
 */
