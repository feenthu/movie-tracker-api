# Movie Tracker API AI-Assisted Development Workflow

This document outlines the AI-assisted development workflow for the Movie Tracker API repository. It provides a structured approach for collaborating with AI to develop features, fix bugs, and implement enhancements.

## Workflow Overview

The workflow consists of these main phases:

1. **Requirement Refinement**: Transforming a high-level requirement into a detailed specification
2. **Implementation Planning**: Creating a step-by-step implementation plan
3. **Code Implementation**: Generating code based on the implementation plan
4. **Testing Strategy**: Creating tests for the implemented code
5. **Documentation**: Updating documentation for the changes

## Folder Structure

All AI-assisted development artifacts are stored within the repository structure:

```
.ai/                        # Main folder for AI workflow documentation
  workflow.md               # This file - main workflow documentation
  templates/                # Templates for various workflow phases
    requirements.md         # Template for requirement specifications
    implementation-plan.md  # Template for implementation plans
    test-plan.md            # Template for test plans
    code-review.md          # Template for code reviews
    
.requirements/              # Folder for specific feature requirements
  feature-name/             # Subfolder for each feature
    specification.md        # Detailed requirement specification
    implementation-plan.md  # Step-by-step implementation plan
    test-plan.md            # Testing strategy and test cases
    code/                   # Generated code for the feature
    tests/                  # Tests for the feature
```

## Phase 1: Requirement Refinement

The requirement refinement phase transforms a high-level request into a detailed specification through a collaborative question-and-answer process.

### Process

1. **Initial Requirement Collection**: 
   - Start with a brief description of the feature or enhancement
   - AI asks clarifying questions to understand the core need

2. **Domain Context Exploration**:
   - Identify how the requirement relates to existing Movie Tracker API concepts
   - Map to ubiquitous language from the domain model (users, movies, authentication, etc.)

3. **Specification Creation**:
   - Collaboratively create a detailed specification document
   - Follow the template in `.ai/templates/requirements.md`
   - Save the final specification in `.requirements/[feature-name]/specification.md`

### Example Questions

- What is the business problem this feature is trying to solve?
- Who are the users/stakeholders of this feature?
- How does this feature integrate with existing functionality?
- What are the acceptance criteria for this feature?
- Are there performance or scalability requirements?
- What security considerations should be addressed?

## Phase 2: Implementation Planning

The implementation planning phase creates a detailed roadmap for implementing the specification.

### Process

1. **Technical Feasibility Analysis**:
   - Analyze existing codebase for similar patterns
   - Identify integration points with existing code
   - Assess technical challenges and considerations

2. **Component Identification**:
   - Identify which components need to be created or modified
   - Map required changes to the repository structure

3. **Step-by-Step Plan Creation**:
   - Create a detailed implementation plan
   - Follow the template in `.ai/templates/implementation-plan.md`
   - Save the final plan in `.requirements/[feature-name]/implementation-plan.md`
   - Create a separate test plan following `.ai/templates/test-plan.md`
   - Save the test plan in `.requirements/[feature-name]/test-plan.md`

### Plan Structure

- **Implementation Plan**:
  - **Prerequisites**: Dependencies, configurations, or setup needed
  - **Component Changes**: List of files to be created or modified
  - **Implementation Steps**: High-level steps with minimal code snippets
  - **Integration Points**: How the new code interfaces with existing code

- **Test Plan** (Separate Document):
  - **Testing Strategy**: Overall approach to testing this feature
  - **Integration Tests**: Recording-based test scenarios
  - **Unit Tests**: Optional tests for complex business logic

## Phase 3: Code Implementation

The code implementation phase executes the steps in the implementation plan.

### Process

1. **File Creation/Modification**:
   - Follow the implementation plan step by step
   - Create or modify code for each component identified in the plan
   - Adhere to the project's coding standards and patterns

2. **Code Quality Checks**:
   - Ensure code follows Java best practices
   - Comply with project-specific guidelines (from `.goosehints`)
   - Follow the established architectural patterns

3. **Implementation Location**:
   - Implement the actual code directly in the main source directories of the project
   - No need to save reference implementations in the `.requirements` directory

> **Important Note**: Unlike the previous phases that create documentation artifacts in the `.requirements` directory, 
> this phase involves implementing actual code in the project's main source directories.
> The implementation plan serves as a guide for the actual implementation.

### Coding Standards

- Use var declarations for variables when type can be inferred
- Use Optional instead of nested null checks
- Use existing mappers if available (see Mapstruct documentation)
- Remove unused imports
- Follow existing patterns in the codebase

## Phase 4: Testing Strategy

The testing strategy phase implements the tests defined in the test plan.

### Process

1. **Test Implementation**:
   - Follow the test plan created in Phase 2
   - Implement tests according to the specified strategy
   - Focus primarily on integration tests using the recording-based approach

2. **Integration Test Implementation**:
   - Create recording-based tests using JSON test case files
   - Organize tests by feature area and operation as defined in the test plan
   - Implement tests directly in the main test directories

3. **Unit Test Implementation (Optional)**:
   - If specified in the test plan, implement unit tests for complex business logic
   - Focus on testing business rules and edge cases
   - Implement tests directly in the main test directories

> **Important Note**: The test plan in the `.requirements` directory serves as a guide for test implementation.
> Actual tests should be implemented directly in the project's main test directories.

### Testing Guidelines

- Focus on integration tests that verify end-to-end behavior
- Use recording-based tests for GraphQL operations
- Store test cases in `test-cases/[feature-area]/[operation]/[scenario].case.json`
- Only write unit tests when they add significant value
- Test both happy path and error scenarios

## Phase 5: Documentation

The documentation phase ensures that all changes are properly documented.

### Process

1. **Code Documentation**:
   - Add appropriate Javadoc comments to new code
   - Update existing documentation as needed

2. **README Updates**:
   - Update README.md if needed
   - Add entries to the ubiquitous language section if appropriate

3. **Sequence Diagrams**:
   - Create or update UML sequence diagrams for new flows
   - Save diagrams in the appropriate location in the docs folder

### Documentation Guidelines

- Keep documentation concise but comprehensive
- Update sequence diagrams for significant workflow changes
- Ensure documentation is consistent with code

## Collaboration Guidelines

For effective collaboration between AI and human developers:

1. **Start with Clear Intent**: Begin with a clear statement of what you want to accomplish
2. **Use Structured Requests**: Frame requests in terms of the workflow phases
3. **Provide Feedback**: Give feedback on AI-generated artifacts
4. **Iterative Refinement**: Expect multiple iterations to get to the final result
5. **Focus on Value**: Let the AI handle routine coding tasks while you focus on design decisions

For example prompts to use with this workflow, see the [prompts.md](prompts.md) file or use the context-based numbered prompt system in the [prompts directory](prompts/).

## Example Workflow

```
Developer: "I need to add a new feature to track basket abandonment events"

AI: [Initiates Requirement Refinement phase]
    "Let's refine this requirement. I'll ask some questions to understand better:
     - What specific events should trigger a basket abandonment?
     - What data needs to be captured when abandonment occurs?
     - How should this data be stored or processed?"

Developer: [Provides answers to refine the requirement]

AI: [Creates specification document in .requirements/basket-abandonment/specification.md]
    "I've created a detailed specification based on our discussion. Please review it."

Developer: [Reviews and approves specification]
    "The specification looks good. Let's proceed with the implementation plan."

AI: [Initiates Implementation Planning phase]
    [Creates implementation plan in .requirements/basket-abandonment/implementation-plan.md]
    "Here's the implementation plan. It covers the components we need to modify and create."

Developer: [Reviews and approves plan]
    "The plan looks good. Let's start implementing."

AI: [Initiates Code Implementation phase]
    [Generates code files according to the plan]
    "I've generated the implementation code. Here's an overview of what I've created..."

Developer: [Reviews code]
    "The code looks good. Let's add tests."

AI: [Initiates Testing Strategy phase]
    [Generates test files]
    "I've created tests for the new functionality..."

Developer: [Reviews tests]
    "The tests look good. Let's make sure our documentation is updated."

AI: [Initiates Documentation phase]
    [Updates documentation]
    "I've updated the documentation to reflect the new feature..."
```

## Conclusion

This workflow provides a structured approach to AI-assisted development for the Basket Service repository. By following these guidelines, developers can collaborate effectively with AI to create high-quality, well-documented code that adheres to the project's standards and architectural patterns.
